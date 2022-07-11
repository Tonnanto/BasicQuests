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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

	static FileConfiguration config = Main.getPlugin().getConfig();

	/**
	 * Saves the default config file to server directory if it does not exist yet.
	 * Overwrites the config file if it is from an older version.
	 */
	public static void update() {

		String configPath = Main.getPlugin().getDataFolder() + File.separator + "config.yml";
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			// No config file exists
			Main.getPlugin().saveDefaultConfig();
			Main.getPlugin().reloadConfig();
			Config.config = Main.getPlugin().getConfig();
			return;
		}

		// Reading old config.yml
		String configString = "";
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(configPath));
			configString = new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			Main.log(Level.SEVERE, "Failed to verify config.yml version");
		}

		// Looking for version String in file
		String version = Main.getPlugin().getDescription().getVersion();
		String versionString = "version " + version;
		Pattern verPat = Pattern.compile("version [0-9.]+\\b"); // TODO: Check
		Matcher m = verPat.matcher(configString);
		if (m.find()) {
			String s = m.group();
			if (s.equalsIgnoreCase(versionString)) {
				// Config is up to date!
				return;
			}
		}
		// Config file needs to be updated

		// Keep old config values to overwrite in new config file
		Map<String, Object> configValues = config.getValues(true);

		// Delete old config.yml
		if (!configFile.delete()) {
			Main.log(Level.SEVERE, "Failed to delete outdated config.yml");
			return;
		}

		// Save new default config.yml
		Main.getPlugin().saveDefaultConfig();

		// Reading new config.yml
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(configPath));
			configString = new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			Main.log(Level.SEVERE, "Failed to read new config.yml file");
		}

		// Replace values in new config.yml with old values
		for (Map.Entry<String, Object> configValue: configValues.entrySet()) {
			Pattern keyPat = Pattern.compile(configValue.getKey() + ":.+\\b");
			Object obj = configValue.getValue();
			configString = keyPat.matcher(configString).replaceAll(configValue.getKey() + ": " + obj.toString());
		}
		configString = verPat.matcher(configString).replaceFirst(versionString);

		// Save new config.yml with replaced values
		File newConfig = new File(configPath);
		try {
			FileWriter fw = new FileWriter(newConfig, false);
			fw.write(configString);
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
			Main.log(Level.SEVERE, "Failed to write to new config.yml file");
			return;
		}
		Main.getPlugin().reloadConfig();
		Config.config = Main.getPlugin().getConfig();

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

//	public static boolean getConsiderJobs() {
//		return config.getBoolean("consider-jobs");
//	}

//	public static double getWeightFactor() {
//		return config.getDouble("job-weight-factor");
//	}

	public static boolean itemRewards() {
		return config.getBoolean("item-rewards");
	}

	public static boolean xpRewards() {
		return config.getBoolean("xp-rewards");
	}

	public static boolean limitProgressMessages() {
		return config.getBoolean("limit-progress-messages");
	}

	public static boolean moneyRewards() {
		return config.getBoolean("money-rewards");
	}

	public static String getLocale() {
		String locale = config.getString("locale");
		if (locale == null) return "en";
		return locale;
	}
}
