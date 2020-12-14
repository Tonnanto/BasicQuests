package de.stamme.basicquests.main;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {

	static FileConfiguration config = Main.plugin.getConfig();
	
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
}
