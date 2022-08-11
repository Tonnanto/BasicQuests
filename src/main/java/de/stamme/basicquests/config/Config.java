package de.stamme.basicquests.config;

import de.stamme.basicquests.BasicQuestsPlugin;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.spigot.SpigotSerializer;

import java.io.File;
import java.io.IOException;

public class Config {
	static YamlDocument config;

	public static void register() {
        BasicQuestsPlugin plugin = BasicQuestsPlugin.getPlugin();

        try {
            config = YamlDocument.create(
                new File(plugin.getDataFolder(), "config.yml"),
                plugin.getResource("config.yml"),
                GeneralSettings.builder().setSerializer(SpigotSerializer.getInstance()).build(),
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build()
            );
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}

    /**
     * Reload the plugin configuration.
     */
    public static void reload() {
        try {
            config.reload();
        } catch (Exception e) {
            //
        }
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
     * Determine whether to limit progress messages.
     *
     * @return boolean
     */
	public static boolean limitProgressMessages() {
		return config.getBoolean("limit-progress-messages");
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
		return config.getInt("minecraft-items-locale-update-period");
	}

    /**
     * Determine whether to show the scoreboard per default.
     *
     * @return boolean
     */
	public static boolean showScoreboardPerDefault() {
		return config.getBoolean("show-scoreboard-per-default");
	}

    /**
     * Determine whether the scoreboard is disabled.
     *
     * @return boolean
     */
	public static boolean isScoreboardDisabled() {
		return config.getBoolean("disable-scoreboard");
	}
}
