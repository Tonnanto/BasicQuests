package de.stamme.basicquests.main;

import de.stamme.basicquests.commands.*;
import de.stamme.basicquests.listeners.*;
import de.stamme.basicquests.quests.FindStructureQuest;
import de.stamme.basicquests.tabcompleter.CompleteQuestTabCompleter;
import de.stamme.basicquests.tabcompleter.QuestsTabCompleter;
import de.stamme.basicquests.tabcompleter.ResetQuestsTabCompleter;
import de.stamme.basicquests.tabcompleter.SkipQuestTabCompleter;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main extends JavaPlugin {
	
	public static Main plugin;
	public static String userdata_path;
	
    private static Economy economy = null;
    private static Permission permissions = null;
    private static Chat chat = null;
	
	public HashMap<UUID, QuestPlayer> questPlayer = new HashMap<>();
	
	@Override
	public void onEnable() {
		plugin = this;
		userdata_path = this.getDataFolder() + "/userdata";

		Plugin vault = getServer().getPluginManager().getPlugin("Vault");

		// Setting up Permissions and Chat with Vault
		if (vault != null) {
			setupPermissions();
			setupChat();
		}

//        Checking reward type from config
		boolean moneyRewards = Config.moneyRewards();
		if ((vault == null || !setupEconomy()) && moneyRewards) {
			log("Money Rewards disabled due to no Vault dependency found!");
			return;
		}

		if (!moneyRewards && !Config.itemRewards() && !Config.xpRewards()) {
			log("Plugin disabled due to no reward type enabled!");
			getServer().getPluginManager().disablePlugin(this);
		}
        
		
        // Loading commands and listeners
		loadCommands();
		loadListeners();

		
		// save default config if not existing - overwrite if config from older version
		Config.update();


		// create userdata directory
		File userfile = new File(userdata_path);
		if (!userfile.exists()) {
			if (!userfile.mkdir()) {
				log(String.format("Failed to create directory %s", userfile.getPath()));
			}
		}
		
		// start schedulers
		this.startPlayerDataSaveScheduler();
		this.startMidnightScheduler();
		FindStructureQuest.startScheduler();
		
		// reload PlayerData for online players
		reloadPlayerData();


		setUpMetrics();

	}
	
	@Override
    public void onDisable() {
        for (Map.Entry<UUID, QuestPlayer> entry: questPlayer.entrySet()) {
        	PlayerData.getPlayerDataAndSave(entry.getValue());
        }
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
		
		Objects.requireNonNull(getCommand("test")).setExecutor(new TestCommand());
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
		pluginManager.registerEvents(new PlayerJoinListener(), this);
		pluginManager.registerEvents(new PlayerQuitListener(), this);
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
	
	// reloads PlayreData for every online player
	private void reloadPlayerData() {
		for (Player player: Bukkit.getServer().getOnlinePlayers()) {
			if (!PlayerData.loadPlayerData(player)) {
				Main.plugin.questPlayer.put(player.getUniqueId(), new QuestPlayer(player));
			}
		}
	}
	
	public static void log(String log) {
		System.out.println("[BasicQuests] " + log);
	}
	
	// starts Scheduler that resets players skip count at midnight
	private void startMidnightScheduler() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0);
		
		if(now.compareTo(nextRun) >= 0)
		    nextRun = nextRun.plusDays(1);

		Duration duration = Duration.between(now, nextRun);
		long initialDelay = duration.getSeconds();

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);            
		scheduler.scheduleAtFixedRate(() -> {
			for (Entry<UUID, QuestPlayer> entry: questPlayer.entrySet()) { // online players
				entry.getValue().setSkipCount(0);
			}

			for (OfflinePlayer player: Bukkit.getServer().getOfflinePlayers()) { // offline players
				PlayerData.resetSkipsForOfflinePlayer(player);
			}

			Main.plugin.getServer().broadcastMessage(String.format("%sQuest skips have been reset!", ChatColor.GOLD));
			Main.log("Quest skips have been reset.");
		},
		    initialDelay,
		    TimeUnit.DAYS.toSeconds(1),
		    TimeUnit.SECONDS);
	}
	
	// start Scheduler that saves PlayerData from online players periodically (10 min)
	private void startPlayerDataSaveScheduler() {
		Bukkit.getScheduler().runTaskTimer(Main.plugin, () -> {
			for (Entry<UUID, QuestPlayer> entry: Main.plugin.questPlayer.entrySet()) {
				PlayerData.getPlayerDataAndSave(entry.getValue());
			}
		}, 12_000L, 12_000L);
	}

	public void setUpMetrics() {
		int pluginId = 9974;
		Metrics metrics = new Metrics(this, pluginId);


		// Add Economy Chart
		metrics.addCustomChart(new Metrics.SimplePie("economy", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return (economy != null) ? "true" : "false";
			}
		}));

		// Add RewardType Chart
		metrics.addCustomChart(new Metrics.SimplePie("reward_type", new Callable<String>() {
			@Override
			public String call() throws Exception {
				ArrayList<String> list = new ArrayList<>();

				if (economy != null && Config.moneyRewards())
					list.add("Money");
				if (Config.itemRewards())
					list.add("Items");
				if (Config.xpRewards())
					list.add("XP");

				return String.join(", ", list);
			}
		}));

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
}
