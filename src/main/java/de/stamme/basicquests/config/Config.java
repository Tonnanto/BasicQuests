package de.stamme.basicquests.config;

import de.stamme.basicquests.BasicQuestsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {
	static FileConfiguration config;
    static String configPath;
    static Pattern versionPattern;

	public static void register() {
        configPath = BasicQuestsPlugin.getPlugin().getDataFolder() + File.separator + "config.yml";
        versionPattern = Pattern.compile("version [0-9.]+\\b");

        File configFile = new File(configPath);
        if (!configFile.exists()) {
            // No config file exists
            BasicQuestsPlugin.getPlugin().saveDefaultConfig();
            BasicQuestsPlugin.getPlugin().reloadConfig();
            config = BasicQuestsPlugin.getPlugin().getConfig();
            return;
        }

        config = BasicQuestsPlugin.getPlugin().getConfig();

        // Reading old config.yml
        String configString = "";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(configPath));
            configString = new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            BasicQuestsPlugin.log(Level.SEVERE, "Failed to read old config file");
        }

        // Looking for version String in file
        Matcher m = versionPattern.matcher(configString);
        if (m.find()) {
            String s = m.group();
            if (s.equalsIgnoreCase(getCurrentVersionString())) {
                // Config is up to date!
                return;
            }
        }
        // Config file needs to be updated
        migrateConfig(configFile);
	}

    /**
     * Migrates an existing config file to a new version
     * 1. Reads old config values
     * 2. Deletes old config file
     * 3. Creates new default config file
     * 4. Injects old values into new file
     * @param oldFile the file to migrate from
     */
	private static void migrateConfig(File oldFile) {
        // Keep old config values to overwrite in new config file
        Map<String, Object> oldValues = config.getValues(true);

        // Delete old config.yml
        if (!oldFile.delete()) {
            BasicQuestsPlugin.log(Level.SEVERE, "Failed to delete outdated config.yml");
        }

        // Save new default config.yml
        BasicQuestsPlugin.getPlugin().saveDefaultConfig();

        // Reading new config.yml
        String configString = "";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(configPath));
            configString = new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            BasicQuestsPlugin.log(Level.SEVERE, "Failed to read new config.yml file");
        }

        // Replace values in new config.yml with old values
        for (Map.Entry<String, Object> configValue: oldValues.entrySet()) {
            Object obj = configValue.getValue();

            if (obj instanceof List) {
                // Replace multiline / list values

                Pattern keyPat = Pattern.compile(configValue.getKey() + ":\\n^\\s*- .*$(\\n^\\s*- .*$)*", Pattern.MULTILINE);
                // Matches consecutive lines starting with "  - " aka lists in the yaml file
                Matcher matcher = keyPat.matcher(configString);
                StringBuilder configValueString = new StringBuilder(configValue.getKey() + ": ");
                ((List<?>) obj).forEach(o -> configValueString.append("\n  - ").append(o.toString()));
                configString = matcher.replaceAll(configValueString.toString());

            } else {
                // Replace single line values
                Pattern keyPat = Pattern.compile(configValue.getKey() + ":.+\\b");
                configString = keyPat.matcher(configString).replaceAll(configValue.getKey() + ": " + obj.toString());
            }
        }
        configString = versionPattern.matcher(configString).replaceFirst(getCurrentVersionString());

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

        BasicQuestsPlugin.log("config.yml has been updated to " + getCurrentVersionString());
    }

    /**
     * @return the current version string in config files
     */
    private static String getCurrentVersionString() {
        String version = BasicQuestsPlugin.getPlugin().getDescription().getVersion();
        return "version " + version;
    }

    /**
     * Reload the plugin configuration.
     */
    public static void reload() {
        BasicQuestsPlugin.getPlugin().reloadConfig();
        config = BasicQuestsPlugin.getPlugin().getConfig();
    }

    /**
     * Retrieve whether to check for updates
     *
     * @return boolean
     */
    public static boolean checkForUpdates() {
        return config.getBoolean("check-for-updates", true);
    }

    /**
     * Retrieve the quest amount.
     *
     * @return int
     */
	public static int getQuestAmount() {
		return config.getInt("quest-amount");
	}

    /**
     * Retrieve the skips per day.
     *
     * @return int
     */
	public static int getSkipsPerDay() {
		return config.getInt("skips-per-day");
	}

    /**
     * Retrieve the money factor.
     *
     * @return double
     */
	public static double getMoneyFactor() {
		return config.getDouble("money-factor");
	}

    /**
     * Retrieve the reward factor.
     *
     * @return double
     */
	public static double getRewardFactor() {
		return config.getDouble("reward-factor");
	}

    /**
     * Retrieve the quantity factor.
     *
     * @return double
     */
	public static double getQuantityFactor() {
		return config.getDouble("quantity-factor");
	}

    /**
     * Determine whether to increase amount by playtime.
     *
     * @return boolean
     */
	public static boolean increaseAmountByPlaytime() {
		return config.getBoolean("increase-quantity-by-playtime");
	}

    /**
     * Retrieve the minimum playtime factor.
     *
     * @return double
     */
	public static double minPlaytimeFactor() {
		return config.getDouble("start-factor");
	}

    /**
     * Retrieve the max playtime factor.
     *
     * @return double
     */
	public static double maxPlaytimeFactor() {
		return config.getDouble("max-factor");
	}

    /**
     * Retrieve the max playtime hours.
     *
     * @return double
     */
	public static double maxPlaytimeHours() {
		return config.getDouble("max-amount-hours");
	}

    /**
     * Determine whether to broadcast on question completion.
     *
     * @return boolean
     */
	public static boolean broadcastOnQuestCompletion() {
		return config.getBoolean("broadcast-on-quest-complete");
	}

    /**
     * Determine whether to play a sound on quest completion.
     *
     * @return boolean
     */
	public static boolean soundOnQuestCompletion() {
		return config.getBoolean("sound-on-quest-complete");
	}

    /**
     * Determine whether to announce a player's new quest(s) when their quests
     * are reset.
     *
     * @return boolean
     */
    public static boolean announceQuestsWhenReset() {
        return config.getBoolean("announce-quests-when-reset");
    }


    /**
     * Determine duplicate quest chance.
     *
     * @return double
     */
	public static double duplicateQuestChance() {
		double val = config.getDouble("duplicate-quest-chance");
		return (val <= 1) ? val : 1.0;
	}

    /**
     * Determine if rewards consist of items.
     *
     * @return boolean
     */
	public static boolean itemRewards() {
		return config.getBoolean("item-rewards");
	}

    /**
     * Determine whether items consist of experience.
     *
     * @return boolean
     */
	public static boolean xpRewards() {
		return config.getBoolean("xp-rewards");
	}

    /**
     * Determine whether rewards consist of money.
     *
     * @return boolean
     */
    public static boolean moneyRewards() {
        return config.getBoolean("money-rewards");
    }

    /**
     * Retrieve the plugin locale.
     *
     * @return String
     */
	public static String getLocale() {
		return config.getString("locale", "en");
	}

    /**
     * Retrieve the Minecraft item locale.
     *
     * @return String
     */
	public static String getMinecraftItemsLocale() {
		String locale = config.getString("minecraft-items-locale");

		return locale == null || locale.equalsIgnoreCase("en_us") ? null : locale;
	}

    /**
     * Retrieve the Minecraft items locale update interval.
     *
     * @return int
     */
	public static int getMinecraftItemsLocaleUpdatePeriod() {
		return 7;
	}

    /**
     * Retrieve whether to show the scoreboard per default.
     *
     * @return boolean
     */
	public static boolean showScoreboardPerDefault() {
		return config.getBoolean("show-scoreboard-per-default");
	}

    /**
     * Retrieve whether the scoreboard is disabled.
     *
     * @return boolean
     */
	public static boolean isScoreboardDisabled() {
		return config.getBoolean("disable-scoreboard");
	}

    /**
     * Retrieve the save interval in mutes.
     *
     * @return int
     */
    public static int getSaveInterval() {
        return config.getInt("save-interval", 10);
    }

    /**
     * Retrieve a list of worlds names where quest progress is banned
     *
     * @return List<String>
     */
    public static List<String> getBannedWorlds() {
        if (config.contains("banned-worlds")) {
            return config.getStringList("banned-worlds");
        }
        return new ArrayList<>();
    }

    /**
     * Return weather quest progress is banned in a world with the given name
     * @param worldName name of the world
     * @return weather quest progress is banned in this world
     */
    public static boolean isWorldBanned(String worldName) {
        if (!getBannedWorlds().isEmpty()) {
            return getBannedWorlds().contains(worldName);
        }
        return false;
    }
}
