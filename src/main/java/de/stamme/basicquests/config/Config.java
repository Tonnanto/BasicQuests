package de.stamme.basicquests.config;

import de.stamme.basicquests.BasicQuestsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public class Config {
	static FileConfiguration config;

	public static void register() {
        config = BasicQuestsPlugin.getPlugin().getConfig();

        config.addDefault("quest-amount", 3);
        config.setComments("quest-amount", Arrays.asList(
            "GENERAL",
            "",
            "amount of quests a player holds at a time",
            "recommended values: min = 3, max = 6"
        ));

        config.addDefault("skips-per-day", 1);
        config.setComments("skips-per-day", Arrays.asList(
            "times a player is allowed to skip a quest. (resets every day)"
        ));

        config.addDefault("broadcast-on-quest-complete", true);
        config.setComments("broadcast-on-question-complete", Arrays.asList(
            "broadcasts a message to everyone if a player completes a quest"
        ));

        config.addDefault("sound-on-quest-complete", true);
        config.setComments("sound-on-question-complete", Arrays.asList(
            "plays a sound when a quest has been completed (only for the player)"
        ));

        config.addDefault("limit-progress-messages", false);
        config.setComments("limit-progress-messages", Arrays.asList(
            "limits progress messages to 4 per quest (25%, 50%, 75%, 100%)"
        ));

        config.addDefault("disable-scoreboard", false);
        config.setComments("disable-scoreboard", Arrays.asList(
            "disable the built-in scoreboard (/quests show command)",
            "quests can always be displayed on custom scoreboards using PlaceholderAPI"
        ));

        config.addDefault("show-scoreboard-per-default", true);
        config.setComments("show-scoreboard-per-default", Arrays.asList(
            "show the scoreboard per default for new players"
        ));

        config.addDefault("locale", "en");
        config.setComments("locale", Arrays.asList(
            "Set the locale for everything except minecraft item names.",
            "Contact me if you would like to have your language supported. You might need to help with translations.",
            "Available locales:",
            "en: English, de: German, es: Spanish, ru: Russian"
        ));

        config.addDefault("minecraft-items-locale", "en_us");
        config.setComments("minecraft-items-locale", Arrays.asList(
            "Set the locale for minecraft item names.",
            "If en_us is set, then the translation file won't be downloaded and default material names will be used.",
            "Available locales: https://minecraft.fandom.com/wiki/Language#Languages (\"In-game\" column.)"
        ));


        config.addDefault("minecraft-items-locale-update-period", 7);
        config.setComments("minecraft-items-locale-update-period", Arrays.asList(
            "Re-download period of the translation file. (In days.)",
            "Set to -1 to disable."
        ));

        config.addDefault("reward-factor", 1.0);
        config.setComments("reward-factor", Arrays.asList(
            "QUEST GENERATION",
            "",
            "factor for the value of rewards",
            "recommended values: min = 0.5, max = 3.0"
        ));

        config.addDefault("quantity-factor", 1.0);
        config.setComments("quantity-factor", Arrays.asList(
            "factor for the quantities in a quest - eg. the amounts of zombies to kill",
            "recommended values: min = 0.5, max = 3.0"
        ));

        config.addDefault("increase-quantity-by-playtime", true);
        config.setComments("increase-quantity-by-playtime", Arrays.asList(
            "Increase the quantities in quests according to a players play time."
        ));

        config.addDefault("start-factor", 0.4);
        config.setComments("start-factor", Arrays.asList(
            "factor when a player joins the game"
        ));

        config.addDefault("max-factor", 3.0);
        config.setComments("max-factor", Arrays.asList(
            "factor when a player reaches <max-amount-hours> hours of playtime."
        ));

        config.addDefault("max-amount-hours", 100);
        config.setComments("max-amount-hours", Arrays.asList(
            "hours of play time which a player receives quests with maximum quantities"
        ));

        config.addDefault("duplicate-quest-chance", 0.3);
        config.setComments("duplicate-quest-chance", Arrays.asList(
            "Chance of duplicate quests [0.0 - 1.0]",
            "0.0: no duplicate quests will appear (not recommended when quest-amount is above 4)",
            "1.0: players active quests haven o influence on the generation of new quests"
        ));

        config.addDefault("item-rewards", true);
        config.setComments("item-rewards", Arrays.asList(
            "REWARDS",
            "",
            "enable items as rewards"
        ));

        config.addDefault("xp-rewards", false);
        config.setComments("xp-rewards", Arrays.asList(
            "enable xp as a reward"
        ));

        config.addDefault("money-rewards", false);
        config.setComments("money-rewards", Arrays.asList(
            "enable money as a reward (requires an economy plugin to be hooked via Vault)"
        ));

        config.addDefault("money-factor", 1.0);
        config.setComments("money-factor", Arrays.asList(
            "ECONOMY",
            "",
            "factor for money rewards",
            "adjust this to the value of money on your server."
        ));

        config.options().copyDefaults(true);

        BasicQuestsPlugin.getPlugin().saveConfig();
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
		return config.getInt("money-factor");
	}

    /**
     * Retrieve the reward factor.
     *
     * @return double
     */
	public static double getRewardFactor() {
		return config.getInt("reward-factor");
	}

    /**
     * Retrieve the quantity factor.
     *
     * @return double
     */
	public static double getQuantityFactor() {
		return config.getInt("quantity-factor");
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
