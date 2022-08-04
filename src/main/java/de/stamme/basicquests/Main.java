package de.stamme.basicquests;

import de.stamme.basicquests.commands.*;
import de.stamme.basicquests.commands.tabcompleter.CompleteQuestTabCompleter;
import de.stamme.basicquests.commands.tabcompleter.QuestsTabCompleter;
import de.stamme.basicquests.commands.tabcompleter.ResetQuestsTabCompleter;
import de.stamme.basicquests.commands.tabcompleter.SkipQuestTabCompleter;
import de.stamme.basicquests.listeners.*;
import de.stamme.basicquests.model.PlayerData;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.FindStructureQuest;
import de.stamme.basicquests.model.wrapper.BukkitVersion;
import de.stamme.basicquests.model.wrapper.material.QuestMaterialService;
import de.stamme.basicquests.util.*;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


public class Main extends JavaPlugin {
	
	private static Main plugin;
	private static String userdataPath;
	private static final int spigotMCID = 87972;
	
    private static Economy economy = null;
    private static Permission permissions = null;
    private static Chat chat = null;
	
	private final HashMap<UUID, QuestPlayer> questPlayers = new HashMap<>();

	@Override
	public void onEnable() {
		plugin = this;
		userdataPath = this.getDataFolder() + "/userdata";


		Plugin vault = getServer().getPluginManager().getPlugin("Vault");

		// Setting up Permissions and Chat with Vault
		if (vault != null) {
			setupPermissions();
			setupChat();
		}

		// Checking reward type from config
		boolean moneyRewards = Config.moneyRewards();
		if ((vault == null || !setupEconomy()) && moneyRewards)
			log("Money Rewards disabled due to no Vault dependency found!");


		if (!moneyRewards && !Config.itemRewards() && !Config.xpRewards()) {
			log("Plugin disabled due to no reward type enabled!");
			getServer().getPluginManager().disablePlugin(this);
		}

		
        // Loading commands and listeners
		loadCommands();
		loadListeners();

		// register PAPI expansion
		registerPapiExpansion();
		
		// save default config if not existing - overwrite if config from older version
		Config.update();

		// init GenerationFileService and save default generation files
		GenerationFileService.getInstance();

		// Set default locale for all messages
		Locale.setDefault(new Locale(Config.getLocale()));

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
			FindStructureQuest.startScheduler();


			// Programmatically set the default permission value cause Bukkit doesn't handle plugin.yml properly for Load order STARTUP plugins
			org.bukkit.permissions.Permission perm = getServer().getPluginManager().getPermission("quests.update");
			if (perm == null)
			{
				perm = new org.bukkit.permissions.Permission("quests.update");
				perm.setDefault(PermissionDefault.OP);
				plugin.getServer().getPluginManager().addPermission(perm);
			}
			perm.setDescription("Allows a user or the console to check for BasicQuests updates");

			getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {

				if (getServer().getConsoleSender().hasPermission("quests.update") && getConfig().getBoolean("update-check", true)) {

					log("Checking for Updates ... ");
					new UpdateChecker(this, spigotMCID).getVersion(version -> {
						String oldVersion = this.getDescription().getVersion();
						if (oldVersion.equalsIgnoreCase(version)) {
							log("No Update available.");
						} else {
							log(String.format("New version (%s) is available! You are using an old version (%s).", version, oldVersion));
						}
					});
				}
			}, 0, 432000);
		});
	}
	
	@Override
    public void onDisable() {
		int successCount = 0;
        for (Map.Entry<UUID, QuestPlayer> entry: questPlayers.entrySet()) {
        	if (PlayerData.getPlayerDataAndSave(entry.getValue()))
        		successCount++;
        }
        Main.log(String.format("Successfully saved PlayerData of %s players%s", successCount, (questPlayers.size() != successCount) ? " (Unsuccessful: " + (questPlayers.size() - successCount) + ")" : ""));
		ServerInfo.save();
    }
	
	private void loadCommands() {
		Objects.requireNonNull(getCommand("quests")).setExecutor(new QuestsCommand());
		Objects.requireNonNull(getCommand("quests")).setTabCompleter(new QuestsTabCompleter());
		Objects.requireNonNull(getCommand("getreward")).setExecutor(new GetRewardCommand());
		Objects.requireNonNull(getCommand("showquests")).setExecutor(new ShowQuestsCommand());
		Objects.requireNonNull(getCommand("hidequests")).setExecutor(new HideQuestsCommand());
		Objects.requireNonNull(getCommand("resetquests")).setExecutor(new ResetQuestsCommand());
		Objects.requireNonNull(getCommand("resetquests")).setTabCompleter(new ResetQuestsTabCompleter());
		Objects.requireNonNull(getCommand("skipquest")).setExecutor(new SkipQuestCommand());
		Objects.requireNonNull(getCommand("skipquest")).setTabCompleter(new SkipQuestTabCompleter());
		Objects.requireNonNull(getCommand("completequest")).setExecutor(new CompleteQuestCommand());
		Objects.requireNonNull(getCommand("completequest")).setTabCompleter(new CompleteQuestTabCompleter());
	}
	
	private void loadListeners() {
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
	}

	private void registerPapiExpansion() {
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new QuestsPlaceholderExpansion(this).register();
		}
	}

	private boolean setupEconomy() {
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
    
    private void setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return;
        }
        chat = rsp.getProvider();
	}
    
    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return;
        }
        permissions = rsp.getProvider();
	}
	
	// reloads PlayerData for every online player
	private void reloadPlayerData() {
		for (Player player: Bukkit.getServer().getOnlinePlayers()) {
			if (!PlayerData.loadPlayerData(player)) {
				Main.getPlugin().getQuestPlayers().put(player.getUniqueId(), new QuestPlayer(player));
			}
		}
	}
	
	public static void log(String message) {
		log(Level.INFO, message);
	}

	public static void log(Level level, String message) {
		Main.getPlugin().getLogger().log(level, message);
	}

	
	// starts Scheduler that resets players skip count at midnight
	private void startMidnightScheduler() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0);
		LocalDateTime lastRun = nextRun;
		LocalDateTime actualLastRun = ServerInfo.getInstance().getLastSkipReset();

		if(now.compareTo(nextRun) >= 0)
		    nextRun = nextRun.plusDays(1);
		else
			lastRun = lastRun.minusDays(1);

		if (actualLastRun == null || Duration.between(actualLastRun, lastRun).getSeconds() > 300) {
			resetAllSkipCounts();
			ServerInfo.getInstance().setLastSkipReset(lastRun);
		}

		Duration duration = Duration.between(now, nextRun);
		long initialDelay = duration.getSeconds();

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);            
		scheduler.scheduleAtFixedRate(this::resetAllSkipCounts,
		    initialDelay,
		    TimeUnit.DAYS.toSeconds(1),
		    TimeUnit.SECONDS);
	}

	private void resetAllSkipCounts() {
		for (Entry<UUID, QuestPlayer> entry: Main.getPlugin().getQuestPlayers().entrySet()) // online players
			entry.getValue().setSkipCount(0);

		for (OfflinePlayer player: Bukkit.getServer().getOfflinePlayers()) // offline players
			PlayerData.resetSkipsForOfflinePlayer(player);

		ServerInfo.getInstance().setLastSkipReset(LocalDateTime.now());
		Main.getPlugin().getServer().broadcastMessage(ChatColor.GOLD + Main.l10n("log.questSkipsReset"));
		Main.log(Main.l10n("log.questSkipsReset"));
	}

	// start Scheduler that saves PlayerData from online players periodically (10 min)
	private void startPlayerDataSaveScheduler() {
		Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
			int successCount = 0;
			for (Entry<UUID, QuestPlayer> entry: Main.getPlugin().getQuestPlayers().entrySet()) {
				if (PlayerData.getPlayerDataAndSave(entry.getValue()))
					successCount++;
			}
			Main.log(String.format("Successfully saved PlayerData of %s players%s", successCount, (questPlayers.size() != successCount) ? " (Unsuccessful: " + (questPlayers.size() - successCount) + ")" : ""));
			ServerInfo.save();
		}, 12_000L, 12_000L);
	}

	// Getters
    public static Economy getEconomy() {
        return economy;
    }
    
    public static Permission getPermissions() {
        return permissions;
    }
    
    public static Chat getChat() {
        return chat;
    }

    public static Main getPlugin() {
		return plugin;
	}

	public static String getUserdataPath() {
		return userdataPath;
	}

	public static int getSpigotMCID() {
		return spigotMCID;
	}

	@NotNull
	public Map<UUID, QuestPlayer> getQuestPlayers() {
		return questPlayers;
	}

	@Nullable
	public QuestPlayer getQuestPlayer(UUID uuid) {
		return questPlayers.get(uuid);
	}

	@Nullable
	public QuestPlayer getQuestPlayer(Player player) {
		if (player == null) return null;
		return getQuestPlayers().get(player.getUniqueId());
	}

	public static String l10n(String messageKey) {
		ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
		return bundle.getString(messageKey);
	}

	public static BukkitVersion getBukkitVersion() {

		if (Main.getPlugin().getServer().getBukkitVersion().contains("1.16"))
			return BukkitVersion.v1_16;

		if (Main.getPlugin().getServer().getBukkitVersion().contains("1.17"))
			return BukkitVersion.v1_17;

		if (Main.getPlugin().getServer().getBukkitVersion().contains("1.18"))
			return BukkitVersion.v1_18;

		// 1.19 or newer
		return BukkitVersion.v1_19;
	}
}
