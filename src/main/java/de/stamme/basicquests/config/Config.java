package de.stamme.basicquests.config;

import de.stamme.basicquests.BasicQuestsPlugin;
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

	static FileConfiguration config;
    private final BasicQuestsPlugin plugin;

    public Config(BasicQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
	 * Saves the default config file to server directory if it does not exist yet.
	 * Overwrites the config file if it is from an older version.
	 */
	public static void update() {
		String configPath = BasicQuestsPlugin.getPlugin().getDataFolder() + File.separator + "config.yml";
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			// No config file exists
			BasicQuestsPlugin.getPlugin().saveDefaultConfig();
			BasicQuestsPlugin.getPlugin().reloadConfig();
			Config.config = BasicQuestsPlugin.getPlugin().getConfig();
			return;
		}
		Config.config = BasicQuestsPlugin.getPlugin().getConfig();

		// Reading old config.yml
		String configString = "";
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(configPath));
			configString = new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			BasicQuestsPlugin.log(Level.SEVERE, "Failed to verify config.yml version");
		}

		// Looking for version String in file
		String version = BasicQuestsPlugin.getPlugin().getDescription().getVersion();
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
			BasicQuestsPlugin.log(Level.SEVERE, "Failed to delete outdated config.yml");
			return;
		}

		// Save new default config.yml
		BasicQuestsPlugin.getPlugin().saveDefaultConfig();

		// Reading new config.yml
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(configPath));
			configString = new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			BasicQuestsPlugin.log(Level.SEVERE, "Failed to read new config.yml file");
		}

		// Replace values in new config.yml with old values
		for (Map.Entry<String, Object> configValue: configValues.entrySet()) {
			Pattern keyPat = Pattern.compile("\n" + configValue.getKey() + ":.+\\b");
			Object obj = configValue.getValue();
			configString = keyPat.matcher(configString).replaceAll("\n" + configValue.getKey() + ": " + obj.toString());
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
			BasicQuestsPlugin.log(Level.SEVERE, "Failed to write to new config.yml file");
			return;
		}
		BasicQuestsPlugin.getPlugin().reloadConfig();
		Config.config = BasicQuestsPlugin.getPlugin().getConfig();

		BasicQuestsPlugin.log("config.yml updated!");
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

	public static double minPlaytimeFactor() {
		return config.getDouble("start-factor");
	}

	public static double maxPlaytimeFactor() {
		return config.getDouble("max-factor");
	}

	public static double maxPlaytimeHours() {
		return config.getDouble("max-amount-hours");
	}

	public static boolean broadcastOnQuestCompletion() {
		return config.getBoolean("broadcast-on-quest-complete");
	}

	public static boolean soundOnQuestCompletion() {
		return config.getBoolean("sound-on-quest-complete");
	}

	public static double duplicateQuestChance() {
		double val = config.getDouble("duplicate-quest-chance");
		return (val <= 1) ? val : 1.0;
	}

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

	public static String getMinecraftItemsLocale() {
		String locale = config.getString("minecraft-items-locale");
		return locale == null || locale.equalsIgnoreCase("en_us") ? null : locale;
	}

	public static int getMinecraftItemsLocaleUpdatePeriod() {
		return config.getInt("minecraft-items-locale-update-period");
	}

	public static boolean showScoreboardPerDefault() {
		return config.getBoolean("show-scoreboard-per-default");
	}

	public static boolean isScoreboardDisabled() {
		return config.getBoolean("disable-scoreboard");
	}
}
