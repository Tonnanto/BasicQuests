package de.stamme.basicquests;

import de.stamme.basicquests.commands.BasicQuestsCommandRouter;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.config.MinecraftLocaleConfig;
import de.stamme.basicquests.listeners.*;
import de.stamme.basicquests.model.PlayerData;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.wrapper.BukkitVersion;
import de.stamme.basicquests.util.*;
import de.themoep.minedown.MineDown;
import net.md_5.bungee.api.ChatMessageType;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BasicQuestsPlugin extends JavaPlugin {
    private static BasicQuestsPlugin plugin;
    private static String userdataPath;
    private static final int spigotMCID = 87972;

    private static Economy economy = null;
    private static Permission permissions = null;
    private static Chat chat = null;

    private final HashMap<UUID, QuestPlayer> questPlayers = new HashMap<>();

    // Handles daily quest skip resets and heads-up broadcasts before reset.
    @Nullable
    ScheduledExecutorService scheduler;

    @Override
    public void onEnable() {
        plugin = this;
        userdataPath = this.getDataFolder() + "/userdata";

        registerConfigs();

        Plugin vault = getServer().getPluginManager().getPlugin("Vault");

        // Setting up Permissions and Chat with Vault
        if (vault != null) {
            registerPermissions();
            registerChat();
        }

        // Checking reward type from config
        boolean moneyRewards = Config.moneyRewards();
        if ((vault == null || !registerEconomy()) && moneyRewards)
            log("Money Rewards disabled due to no Vault dependency found!");


        if (!moneyRewards && !Config.itemRewards() && !Config.xpRewards()) {
            log("Plugin disabled due to no reward type enabled!");
            getServer().getPluginManager().disablePlugin(this);
        }

        // Loading commands and listeners
        registerCommands();
        registerListeners();

        // register PAPI expansion
        registerPapiExpansion();

        // init GenerationFileService and save default generation files
        GenerationFileService.getInstance();

        // create userdata directory
        File userFile = new File(userdataPath);
        if (!userFile.exists()) {
            if (!userFile.mkdir()) {
                log(String.format("Failed to create directory %s", userFile.getPath()));
            }
        }

        MetricsService.setUpMetrics();

        // run after reload is complete
        getServer().getScheduler().runTask(this, () -> {
            // reload server info
            ServerInfo.getInstance();

            // reload PlayerData for online players
            reloadPlayerData();

            // start schedulers
            startPlayerDataSaveScheduler();
            startMidnightScheduler();
            Quest.startProgressScheduler();

            // Programmatically set the default permission value cause Bukkit doesn't handle plugin.yml properly for Load order STARTUP plugins
            org.bukkit.permissions.Permission perm = getServer().getPluginManager().getPermission("basicquests.admin.update");

            if (perm == null) {
                perm = new org.bukkit.permissions.Permission("basicquests.admin.update");
                perm.setDefault(PermissionDefault.OP);
                plugin.getServer().getPluginManager().addPermission(perm);
            }

            perm.setDescription("Allows a user or the console to check for BasicQuests updates");

            UpdateChecker.getInstance();
        });
    }

    @Override
    public void onDisable() {
        if (scheduler != null)
            scheduler.shutdown();

        int playerSaveSuccessCount = 0;
        for (Map.Entry<UUID, QuestPlayer> entry : questPlayers.entrySet()) {
            if (PlayerData.getPlayerDataAndSave(entry.getValue()))
                playerSaveSuccessCount++;
        }
        if (playerSaveSuccessCount > 0) {
            BasicQuestsPlugin.log(
                String.format("Successfully saved PlayerData of %s players%s",
                    playerSaveSuccessCount,
                    (questPlayers.size() != playerSaveSuccessCount) ? " (Unsuccessful: " + (questPlayers.size() - playerSaveSuccessCount) + ")" : "")
            );
        }

        ServerInfo.save();
    }

    /**
     * Register the plugin configuration.
     */
    private void registerConfigs() {
        Config.register();
        MessagesConfig.register(Config.getLocale());
        MinecraftLocaleConfig.register();
    }

    /**
     * Register the plugin commands.
     */
    private void registerCommands() {
        final PluginCommand pluginCommand = getCommand("basicquests");

        if (pluginCommand == null) {
            return;
        }

        final BasicQuestsCommandRouter router = new BasicQuestsCommandRouter(this);

        pluginCommand.setExecutor(router);
        pluginCommand.setTabCompleter(router);
    }

    /**
     * Register the plugin listeners.
     */
    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new BreakBlockListener(), this);
        pluginManager.registerEvents(new BlockPlaceListener(), this);
        pluginManager.registerEvents(new HarvestBlockListener(), this);
        pluginManager.registerEvents(new EntityDeathListener(), this);
        pluginManager.registerEvents(new EnchantItemListener(), this);
        pluginManager.registerEvents(new PlayerLevelChangeListener(), this);
        pluginManager.registerEvents(new BlockDropItemListener(), this);
        pluginManager.registerEvents(new InventoryClickListener(), this);
        pluginManager.registerEvents(new InventoryCloseListener(), this);
        pluginManager.registerEvents(new PlayerJoinListener(), this);
        pluginManager.registerEvents(new PlayerQuitListener(), this);
        pluginManager.registerEvents(new PlayerFishListener(), this);
        pluginManager.registerEvents(new IncreaseStatListener(), this);
    }

    /**
     * Register the PlaceholderAPI expansion.
     */
    private void registerPapiExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new QuestsPlaceholderExpansion(this).register();
        }
    }

    /**
     * Register the economy instance.
     *
     * @return boolean
     */
    private boolean registerEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();

        return true;
    }

    /**
     * Register the chat instance.
     */
    private void registerChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);

        if (rsp == null) {
            return;
        }

        chat = rsp.getProvider();
    }

    /**
     * Register the permissions instance.
     */
    private void registerPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return;
        }
        permissions = rsp.getProvider();
    }

    /**
     * Reload the player data.
     */
    private void reloadPlayerData() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!PlayerData.loadPlayerData(player)) {
                BasicQuestsPlugin.getPlugin().getQuestPlayers().put(player.getUniqueId(), new QuestPlayer(player));
            }
        }
    }

    /**
     * Log a message to console.
     *
     * @param message The message.
     */
    public static void log(String message) {
        log(Level.INFO, message);
    }

    /**
     * Log a message to console.
     *
     * @param level   The log level.
     * @param message The message.
     */
    public static void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    /**
     * Initialize the midnight scheduler.
     */
    private void startMidnightScheduler() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastRun = nextRun;
        LocalDateTime actualLastRun = ServerInfo.getInstance().getLastSkipReset();

        if (!now.isBefore(nextRun))
            nextRun = nextRun.plusDays(1);
        else
            lastRun = lastRun.minusDays(1);

        if (actualLastRun == null || Duration.between(actualLastRun, lastRun).getSeconds() > 300) {
            resetAllSkipCounts();
            ServerInfo.getInstance().setLastSkipReset(lastRun);
        }

        Duration durationUntilRest = Duration.between(now, nextRun);
        long initialResetDelay = durationUntilRest.getSeconds();

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::resetAllSkipCounts,
            initialResetDelay,
            TimeUnit.DAYS.toSeconds(1),
            TimeUnit.SECONDS);

        // Start scheduler for info broadcast 30 minutes before reset
        Duration durationUntilInfo = durationUntilRest.minusMinutes(30);
        if (durationUntilInfo.isNegative()) {
            durationUntilInfo = durationUntilInfo.plusDays(1);
        }
        long initialInfoDelay = durationUntilInfo.getSeconds();
        scheduler.scheduleAtFixedRate(this::skipsResetInfoBroadcast,
            initialInfoDelay,
            TimeUnit.DAYS.toSeconds(1),
            TimeUnit.SECONDS);
    }

    /**
     * Reset all skip counts.
     */
    private void resetAllSkipCounts() {
        for (Entry<UUID, QuestPlayer> entry : BasicQuestsPlugin.getPlugin().getQuestPlayers().entrySet()) // online players
            entry.getValue().setSkipCount(0);

        for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) // offline players
            PlayerData.resetSkipsForOfflinePlayer(player);

        ServerInfo.getInstance().setLastSkipReset(LocalDateTime.now());

        BasicQuestsPlugin.broadcastMessage(
            MessagesConfig.getMessage("events.log.skips-reset")
        );

        BasicQuestsPlugin.log(MessagesConfig.getMessage("events.log.skips-reset"));
    }

    /**
     * Broadcasts a message to all players that have skips left 30 minutes before the reset.
     */
    private void skipsResetInfoBroadcast() {
        for (Entry<UUID, QuestPlayer> entry : BasicQuestsPlugin.getPlugin().getQuestPlayers().entrySet()) {
            int skipsLeft = entry.getValue().getSkipsLeft();

            // Show message only if player has skips left
            if (skipsLeft > 0) {
                // Only show number of remaining skips if player has no permission for infinite skips
                if (!entry.getValue().hasPermission("basicquests.admin.skip.unlimited") &&
                    !entry.getValue().hasPermission("basicquests.admin.skip.others")) {
                    BasicQuestsPlugin.sendMessage(entry.getValue().getPlayer(), MessageFormat.format(MessagesConfig.getMessage("commands.skip.remaining"), skipsLeft, StringFormatter.formatSkips(skipsLeft)));
                }
                BasicQuestsPlugin.sendMessage(entry.getValue().getPlayer(), MessageFormat.format(MessagesConfig.getMessage("commands.skip.heads-up"), StringFormatter.timeToMidnight()));
            }
        }
    }

    /**
     * Initialize the player data save scheduler.
     */
    private void startPlayerDataSaveScheduler() {
        long saveInterval = Config.getSaveInterval() * 1200L;
        Bukkit.getScheduler().runTaskTimer(BasicQuestsPlugin.getPlugin(), () -> {
            int successCount = 0;
            for (Entry<UUID, QuestPlayer> entry : BasicQuestsPlugin.getPlugin().getQuestPlayers().entrySet()) {
                if (PlayerData.getPlayerDataAndSave(entry.getValue()))
                    successCount++;
            }
            if (successCount > 0) {
                BasicQuestsPlugin.log(String.format("Successfully saved PlayerData of %s players%s", successCount, (questPlayers.size() != successCount) ? " (Unsuccessful: " + (questPlayers.size() - successCount) + ")" : ""));
            }
            ServerInfo.save();
        }, saveInterval, saveInterval);
    }

    /**
     * Send an action bar message formatted with MineDown.
     *
     * @param player The player.
     * @param value  The message.
     */
    public static void sendActionMessage(Player player, String value) {
        player.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            MineDown.parse(value)
        );
    }

    /**
     * Broadcast a message formatted with MineDown.
     *
     * @param value The message.
     */
    public static void broadcastMessage(String value) {
        getPlugin().getServer().getOnlinePlayers().forEach(player -> sendMessage(player, value));
    }

    /**
     * Send a message formatted with MineDown.
     *
     * @param sender The command sender.
     * @param value  The message.
     */
    public static void sendMessage(CommandSender sender, String value) {
        sender.spigot().sendMessage(
            MineDown.parse(MessagesConfig.getMessage("generic.prefix") + value)
        );
    }

    /**
     * Send a raw message formatted with MineDown.
     *
     * @param sender The command sender.
     * @param value  The message.
     */
    public static void sendRawMessage(CommandSender sender, String value) {
        sender.spigot().sendMessage(
            MineDown.parse(value)
        );
    }

    /**
     * Retrieve the economy instance.
     *
     * @return Economy
     */
    public static Economy getEconomy() {
        return economy;
    }

    /**
     * Retrieve the permissions instance.
     *
     * @return Permission
     */
    public static Permission getPermissions() {
        return permissions;
    }

    /**
     * Retrieve the chat instance.
     *
     * @return Chat
     */
    public static Chat getChat() {
        return chat;
    }

    /**
     * Retrieve the plugin instance.
     *
     * @return BasicQuestsPlugin
     */
    public static BasicQuestsPlugin getPlugin() {
        return plugin;
    }

    /**
     * Retrieve the userdata path.
     *
     * @return String
     */
    public static String getUserdataPath() {
        return userdataPath;
    }

    /**
     * Retrieve the Spigot plugin ID.
     *
     * @return int
     */
    public static int getSpigotMCID() {
        return spigotMCID;
    }

    /**
     * Retrieve the quest players.
     *
     * @return Map
     */
    @NotNull
    public Map<UUID, QuestPlayer> getQuestPlayers() {
        return questPlayers;
    }

    /**
     * Retrieve the quest player.
     *
     * @param uuid The player's UUID.
     * @return QuestPlayer
     */
    @Nullable
    public QuestPlayer getQuestPlayer(UUID uuid) {
        return questPlayers.get(uuid);
    }

    /**
     * Retrieve the quest player.
     *
     * @param player The player.
     * @return QuestPlayer
     */
    @Nullable
    public QuestPlayer getQuestPlayer(Player player) {
        if (player == null) return null;
        return getQuestPlayers().get(player.getUniqueId());
    }

    /**
     * Retrieve the Bukkit version.
     *
     * @return BukkitVersion
     */
    public static BukkitVersion getBukkitVersion() {
        if (BasicQuestsPlugin.getPlugin().getServer().getBukkitVersion().contains("1.16"))
            return BukkitVersion.v1_16;

        if (BasicQuestsPlugin.getPlugin().getServer().getBukkitVersion().contains("1.17"))
            return BukkitVersion.v1_17;

        if (BasicQuestsPlugin.getPlugin().getServer().getBukkitVersion().contains("1.18"))
            return BukkitVersion.v1_18;

        if (BasicQuestsPlugin.getPlugin().getServer().getBukkitVersion().contains("1.19"))
            return BukkitVersion.v1_19;

        return BukkitVersion.v1_20;
    }

    /**
     * Reload the plugin configuration and quest generation files.
     */
    public void reload() {
        Config.reload();
        MessagesConfig.register(Config.getLocale());
        MinecraftLocaleConfig.register();
        GenerationFileService.reload();

        questPlayers.forEach((uuid, questPlayer) -> questPlayer.receiveNewQuests());
    }
}
