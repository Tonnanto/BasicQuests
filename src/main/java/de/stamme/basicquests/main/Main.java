package de.stamme.basicquests.main;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;

import de.stamme.basicquests.commands.CompleteQuestCommand;
import de.stamme.basicquests.commands.GetRewardCommand;
import de.stamme.basicquests.commands.HideQuestsCommand;
import de.stamme.basicquests.commands.QuestsCommand;
import de.stamme.basicquests.commands.ResetQuestsCommand;
import de.stamme.basicquests.commands.ResetQuestsTabCompleter;
import de.stamme.basicquests.commands.ShowQuestsCommand;
import de.stamme.basicquests.commands.SkipQuestCommand;
import de.stamme.basicquests.commands.TestCommand;
import de.stamme.basicquests.listeners.BlockDropItemListener;
import de.stamme.basicquests.listeners.BlockPlaceListener;
import de.stamme.basicquests.listeners.BreakBlockListener;
import de.stamme.basicquests.listeners.EnchantItemListener;
import de.stamme.basicquests.listeners.EntityDeathListener;
import de.stamme.basicquests.listeners.HarvestBlockListener;
import de.stamme.basicquests.listeners.InventoryClickListener;
import de.stamme.basicquests.listeners.PlayerJoinListener;
import de.stamme.basicquests.listeners.PlayerLevelChangeListener;
import de.stamme.basicquests.listeners.PlayerQuitListener;
import de.stamme.basicquests.quests.FindStructureQuest;
import de.stamme.basicquests.tabcompleter.CompleteQuestTabCompleter;
import de.stamme.basicquests.tabcompleter.QuestsTabCompleter;
import de.stamme.basicquests.tabcompleter.SkipQuestTabCompleter;
import net.md_5.bungee.api.ChatColor;


public class Main extends JavaPlugin {
	
	public static Main plugin;
	public static Essentials essentials;
	public static String userdata_path;
	
	public HashMap<UUID, QuestPlayer> questPlayer = new HashMap<>();
	
	public void onEnable() {
		plugin = this;
		userdata_path = this.getDataFolder() + "/userdata";
		
		loadCommands();
		loadListeners();
		
		// Dependent Plug-ins
		PluginManager pluginManager = Bukkit.getPluginManager();
		essentials = (Essentials) pluginManager.getPlugin("Essentials");

		
		// save default config if not existing
		File config = new File("config.yml");
		if (!config.exists()) {
			saveDefaultConfig();
		}
		
		// create userdata directory
		File userfile = new File(userdata_path);
		if (!userfile.exists()) {
			userfile.mkdir();
		}
		
		// start schedulers
		this.startMidnightScheduler();
		FindStructureQuest.startScheduler();

	}
	
	private void loadCommands() {
		getCommand("quests").setExecutor(new QuestsCommand());
		getCommand("quests").setTabCompleter(new QuestsTabCompleter());
		getCommand("getreward").setExecutor(new GetRewardCommand());
		getCommand("showquests").setExecutor(new ShowQuestsCommand());
		getCommand("hidequests").setExecutor(new HideQuestsCommand());
		getCommand("resetquests").setExecutor(new ResetQuestsCommand());
		getCommand("resetquests").setTabCompleter(new ResetQuestsTabCompleter());
		getCommand("skipquest").setExecutor(new SkipQuestCommand());
		getCommand("skipquest").setTabCompleter(new SkipQuestTabCompleter());
		getCommand("completequest").setExecutor(new CompleteQuestCommand());
		getCommand("completequest").setTabCompleter(new CompleteQuestTabCompleter());
		
		getCommand("test").setExecutor(new TestCommand());
	}
	
	private void loadListeners() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new BreakBlockListener(), this);
		pluginManager.registerEvents(new BlockPlaceListener(), this);
		pluginManager.registerEvents(new HarvestBlockListener(), this);
		pluginManager.registerEvents(new EntityDeathListener(), this);
		pluginManager.registerEvents(new EnchantItemListener(), this);
		pluginManager.registerEvents(new InventoryClickListener(), this);
		pluginManager.registerEvents(new PlayerLevelChangeListener(), this);
		pluginManager.registerEvents(new BlockDropItemListener(), this);
		
		pluginManager.registerEvents(new PlayerJoinListener(), this);
		pluginManager.registerEvents(new PlayerQuitListener(), this);
	}
	

	
	public static void log(String log) {
		System.out.println("[BasicQuests] " + log);
	}
	
	public void startMidnightScheduler() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0);
		
		if(now.compareTo(nextRun) >= 0)
		    nextRun = nextRun.plusDays(1);

		Duration duration = Duration.between(now, nextRun);
		long initalDelay = duration.getSeconds();

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);            
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
		    public void run() {
				for (HashMap.Entry<UUID, QuestPlayer> entry: questPlayer.entrySet()) {
					entry.getValue().setSkipCount(0);
					Main.plugin.getServer().broadcastMessage(String.format("Quest skipps have been reset!", ChatColor.GOLD));
				}
			}
		},
		    initalDelay,
		    TimeUnit.DAYS.toSeconds(1),
		    TimeUnit.SECONDS);
	}
}
