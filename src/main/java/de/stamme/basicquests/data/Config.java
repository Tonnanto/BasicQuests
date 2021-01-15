package de.stamme.basicquests.data;

import de.stamme.basicquests.main.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

	static FileConfiguration config = Main.plugin.getConfig();

	public static void update() {

		String configPath = Main.plugin.getDataFolder() + File.separator + "config.yml";
		File configFile = new File(configPath);
		if (!configFile.exists()) {
//			No config file exists
			Main.plugin.saveDefaultConfig();
			Main.plugin.reloadConfig();
			Config.config = Main.plugin.getConfig();
			return;
		}

//		Reading old config.yml
		String configString = "";
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(configPath));
			configString = new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			Main.log("Failed to verify config.yml version");
		}

//		Looking for version String in file
		String version = Main.plugin.getDescription().getVersion();
		String versionString = "version " + version;
		Pattern verPat = Pattern.compile("version [0-9.]+\\b");
		Matcher m = verPat.matcher(configString);
		if (m.find()) {
			String s = m.group();
			if (s.equalsIgnoreCase(versionString)) {
//				Config is up to date!
				return;
			}
		}

//		Config file needs to be updated
		Map<String, Object> entries = config.getValues(true);

		if (!configFile.delete()) {
			Main.log("Failed to delete outdated config.yml");
			return;
		}

		Main.plugin.saveDefaultConfig();

//		Reading new config.yml
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(configPath));
			configString = new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			Main.log("Failed to read new config.yml file");
		}

		for (Map.Entry<String, Object> entry: entries.entrySet()) {
			Pattern keyPat = Pattern.compile(entry.getKey() + ":.+\\b");
			Object obj = entry.getValue();
			configString = keyPat.matcher(configString).replaceAll(entry.getKey() + ": " + obj.toString());
		}
		configString = verPat.matcher(configString).replaceFirst(versionString);

		File newConfig = new File(configPath);
		try {
			FileWriter fw = new FileWriter(newConfig, false);
			fw.write(configString);
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
			Main.log("Failed to write to new config.yml file");
			return;
		}
		Main.plugin.reloadConfig();
		Config.config = Main.plugin.getConfig();

		Main.log("config.yml updated!");
	}
	
	public static int getQuestAmount() {
		return config.getInt("quest-amount");
	}
	
	public static int getSkipsPerDay() {
		return config.getInt("skips-per-day");
	}
	
	public static double getMoneyFactor() {
		return config.getInt("money-factor");
	}
	
	public static double getRewardFactor() {
		return config.getInt("reward-factor");
	}
	
	public static double getQuantityFactor() {
		return config.getInt("quantity-factor");
	}
	
	public static boolean increaseAmountByPlaytime() {
		return config.getBoolean("increase-quantity-by-playtime");
	}
	
	public static boolean broadcastOnQuestCompletion() {
		return config.getBoolean("broadcast-on-quest-complete");
	}
	
	public static double duplicateQuestChance() {
		double val = config.getDouble("duplicate-quest-chance");
		return (val <= 1) ? val : 1.0;
	}

	public static boolean getConsiderJobs() {
		return config.getBoolean("consider-jobs");
	}

	public static double getWeightFactor() {
		return config.getDouble("job-weight-factor");
	}

	public static boolean itemRewards() {
		return config.getBoolean("item-rewards");
	}

	public static boolean xpRewards() {
		return config.getBoolean("xp-rewards");
	}

	public static boolean moneyRewards() {
		return config.getBoolean("money-rewards");
	}
}
