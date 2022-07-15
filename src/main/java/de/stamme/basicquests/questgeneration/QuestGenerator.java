package de.stamme.basicquests.questgeneration;

import de.stamme.basicquests.Config;
import de.stamme.basicquests.model.generation.GenerationConfig;
import de.stamme.basicquests.model.generation.GenerationOption;
import de.stamme.basicquests.model.generation.QuestGenerationException;
import de.stamme.basicquests.model.rewards.RewardType;
import de.stamme.basicquests.util.GenerationFileService;
import de.stamme.basicquests.model.wrapper.QuestStructureType;
import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.*;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class QuestGenerator {

	private static QuestGenerator instance;

	public static QuestGenerator getInstance() {
		if (instance == null)
			instance = new QuestGenerator();
		return instance;
	}

	private QuestGenerator() {}
	
	// Main Method only for Testing Purpose
//	public static void main(String[] args) {
//
//
//		Random r = new Random();
//		
//		int min = 40, max = 200, mean = 60;
//		for (int i = 0; i < 100; i++) {
//			double x = r.nextGaussian();
//			double v;
//			
//			if (x < 0) {
//				v = x * (mean - min) / 2.5 + mean;
//			} else {
//				v = x * (max - mean) / 2.5 + mean;
//			}
//			
//			System.out.println(v);
//		}
//	}

	/**
	 * randomly decides for an object based on the given weight
	 * @param objects objects to decide from
	 * @return the decided object
	 */
	public GenerationOption decide(List<GenerationOption> objects) {
		double x = 0;
		double tot = objects.stream()
				.map(GenerationOption::getWeight)
				.mapToDouble(Double::doubleValue)
				.sum();
		
		Random r = new Random();
		double target = tot * r.nextDouble();
		
		for (GenerationOption obj: objects) {
			x += obj.getWeight();
			if (x >= target) { return obj; }
		}
		
		return null;
	}
	
	public GenerationOption decide(List<GenerationOption> objects, QuestPlayer questPlayer) {
//		boolean consider_jobs = Config.getConsiderJobs();
//		double job_weight_factor = Config.getWeightFactor();
		
		for (GenerationOption obj: objects) {
			
			// set DecisionObjects weight to 0 if a required advancement has not been made (eg. "story/mine_diamond")
			if (obj.getAdvancements() != null) {
				for (String key: obj.getAdvancements()) {
					key = key.replace(".", "/");
					Advancement adv = Bukkit.getAdvancement(NamespacedKey.minecraft(key));
					if (adv != null && !questPlayer.getPlayer().getAdvancementProgress(adv).isDone()) {
						obj.setWeight(0);
					}
				}
			}
			
			// Reduce weight of Decision Objects that are already in the players quests
			if (questPlayer.getQuests() != null) {
				for (Quest quest: questPlayer.getQuests()) {
					for (String name: quest.getDecisionObjectNames()) {
						if (obj.getName().equalsIgnoreCase(name)) {
							obj.setWeight(obj.getWeight() * Config.duplicateQuestChance());
							break;
						}
					}
				}
			}
			
//			if (consider_jobs) {
				// TODO: adjust DecisionObjects weight if player holds a tagged job
//			}
		}
		
		return decide(objects);
	}
	
	public Quest generate(QuestPlayer questPlayer) throws QuestGenerationException {

		double reward_factor = Config.getRewardFactor();
		double amount_factor = Config.getQuantityFactor();

		if (Config.increaseAmountByPlaytime())
			amount_factor *= getPlaytimeAmountFactor(questPlayer.getPlayer());

		Quest quest = null;
		GenerationConfig generationConfig = GenerationFileService.getInstance().getQuestTypeGenerationConfig();

		assert generationConfig.getOptions() != null;
		GenerationOption questTypeOption = decide(generationConfig.getOptions(), questPlayer);

		QuestType questType;

		try {
			questType = QuestType.valueOf(questTypeOption.getName());
		} catch(IllegalArgumentException exception) {
			// QuestType was not found
			throw new QuestGenerationException(String.format("QuestType '%s' does not exist.", questTypeOption.getName()));
		}

		switch (questType) {
			case BREAK_BLOCK:
				quest = generateBreakBlockQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
				break;
			case MINE_BLOCK:
				quest = generateMineBlockQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
				break;
			case HARVEST_BLOCK:
				quest = generateHarvestBlockQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
				break;
			case ENCHANT_ITEM:
				quest = generateEnchantItemQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
				break;
			case KILL_ENTITY:
				quest = generateKillEntityQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
				break;
			case GAIN_LEVEL:
				quest = generateGainLevelQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
				break;
			case REACH_LEVEL:
				quest = generateReachLevelQuest(questPlayer, questTypeOption.getValue() * reward_factor);
				break;
			case FIND_STRUCTURE:
				quest = generateFindStructureQuest(questPlayer, questTypeOption.getValue() * reward_factor);
				break;
			case CHOP_WOOD:
				quest = generateChopWoodQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
				break;
		}

		// Prevent null quests
		if (quest == null) quest = generate(questPlayer);
		
		return quest;
	}


	// ---------------------------------------------------------------------------------------
	// Break Block Quest
	// ---------------------------------------------------------------------------------------

	Quest generateBreakBlockQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
		GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.BREAK_BLOCK);

		assert generationConfig.getOptions() != null;
		GenerationOption materialOption = decide(generationConfig.getOptions(), questPlayer);
		Material materialToBreak;

		try {
			materialToBreak = Material.valueOf(materialOption.getName());
		} catch(IllegalArgumentException exception) {
			// If Material was not found
			Main.log(Level.SEVERE,String.format("Material '%s' does not exist.", materialOption.getName()));
			return generate(questPlayer);
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToBreak = generateAmount(materialOption, generationConfig, amount_factor);

		double value = materialOption.getValue(amountToBreak) * reward_factor;
		Reward reward = generateReward(QuestType.BREAK_BLOCK, value, questPlayer);

		return new BlockBreakQuest(materialToBreak, amountToBreak, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Mine Block Quest
	// ---------------------------------------------------------------------------------------

	Quest generateMineBlockQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
		GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.MINE_BLOCK);

		assert generationConfig.getOptions() != null;
		GenerationOption materialOption = decide(generationConfig.getOptions(), questPlayer);
		Material materialToMine;

		try {
			materialToMine = Material.valueOf(materialOption.getName());
		} catch(IllegalArgumentException exception) {
			// If Material was not found
			Main.log(Level.SEVERE,String.format("Material '%s' does not exist.", materialOption.getName()));
			return generate(questPlayer);
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToMine = generateAmount(materialOption, generationConfig, amount_factor);

		double value = materialOption.getValue(amountToMine) * reward_factor;
		Reward reward = generateReward(QuestType.MINE_BLOCK, value, questPlayer);

		return new MineBlockQuest(materialToMine, amountToMine, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Harvest Block Quest
	// ---------------------------------------------------------------------------------------

	Quest generateHarvestBlockQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
		GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.HARVEST_BLOCK);

		assert generationConfig.getOptions() != null;
		GenerationOption materialOption = decide(generationConfig.getOptions(), questPlayer);
		Material materialToHarvest;

		try {
			materialToHarvest = Material.valueOf(materialOption.getName());
		} catch(IllegalArgumentException exception) {
			// If Material was not found
			Main.log(Level.SEVERE,String.format("Material '%s' does not exist.", materialOption.getName()));
			return generate(questPlayer);
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToHarvest = generateAmount(materialOption, generationConfig, amount_factor);

		double value = materialOption.getValue(amountToHarvest) * reward_factor;
		Reward reward = generateReward(QuestType.HARVEST_BLOCK, value, questPlayer);

		return new HarvestBlockQuest(materialToHarvest, amountToHarvest, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Chop Wood Quest
	// ---------------------------------------------------------------------------------------

	Quest generateChopWoodQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
		GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.CHOP_WOOD);

		assert generationConfig.getOptions() != null;
		GenerationOption woodOption = decide(generationConfig.getOptions(), questPlayer);
		Material woodToChop = null;

		if (!woodOption.getName().equalsIgnoreCase("LOG")) {
			try {
				woodToChop = Material.valueOf(woodOption.getName());
			} catch(IllegalArgumentException exception) {
				// If Material was not found
				Main.log(Level.SEVERE,String.format("Material '%s' does not exist.", woodOption.getName()));
				return generate(questPlayer);
			}
		}

		// Check if Material was found   OR   material.name == LOG
		if (woodToChop == null && !woodOption.getName().equalsIgnoreCase("LOG")) {
			Main.log(Level.SEVERE,String.format("Material '%s' does not exist.", woodOption.getName()));
			return generate(questPlayer);
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToChop = generateAmount(woodOption, generationConfig, amount_factor);
		double value = woodOption.getValue(amountToChop) * reward_factor;
		Reward reward = generateReward(QuestType.CHOP_WOOD, value, questPlayer);

		if (woodOption.getName().equalsIgnoreCase("LOG")) { // General Log quest that accepts all kind of logs
			return new ChopWoodQuest(woodOption.getName(), amountToChop, reward);
		} else {
			return new ChopWoodQuest(woodToChop, amountToChop, reward);
		}
	}


	// ---------------------------------------------------------------------------------------
	// Kill Entity Quest
	// ---------------------------------------------------------------------------------------

	Quest generateKillEntityQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
		GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.KILL_ENTITY);

		assert generationConfig.getOptions() != null;
		GenerationOption entityOption = decide(generationConfig.getOptions(), questPlayer);
		EntityType entityToKill;

		try {
			entityToKill =  EntityType.valueOf(entityOption.getName());
		} catch(IllegalArgumentException exception) {
			// If Entity was not found
			Main.log(Level.SEVERE,String.format("Entity '%s' does not exist.", entityOption.getName()));
			return generate(questPlayer);
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToKill = generateAmount(entityOption, generationConfig, amount_factor);

		double value = entityOption.getValue(amountToKill) * reward_factor;
		Reward reward = generateReward(QuestType.KILL_ENTITY, value, questPlayer);

		return new EntityKillQuest(entityToKill, amountToKill, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Enchant Item Quest
	// ---------------------------------------------------------------------------------------

	Quest generateEnchantItemQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
		GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.ENCHANT_ITEM);

		assert generationConfig.getOptions() != null;
		GenerationOption materialOption = decide(generationConfig.getOptions(), questPlayer);
		Material itemToEnchant;

		try {
			itemToEnchant = Material.valueOf(materialOption.getName());
		} catch (IllegalArgumentException exception) {
			// If Material was not found
			Main.log(Level.SEVERE,String.format("Material '%s' does not exist.", materialOption.getName()));
			return generate(questPlayer);
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToEnchant = generateAmount(materialOption, generationConfig, amount_factor);


		if (materialOption.getOptions() != null) { // When Enchantments are available

			GenerationOption enchantmentOption = decide(materialOption.getOptions());
			assert enchantmentOption != null;

			NamespacedKey enchantmentKey = NamespacedKey.minecraft(enchantmentOption.getName().toLowerCase());
//			Enchantment enchantment = new EnchantmentWrapper(enchantmentOption.name.toLowerCase());
			Enchantment enchantment = EnchantmentWrapper.getByKey(enchantmentKey);


			if (enchantment != null) {
				int enchantmentLevel = 1;
				if (enchantment.getMaxLevel() > 2) {
					Random r = new Random();
					enchantmentLevel = r.nextInt(enchantment.getMaxLevel() - 1) + 1;
				}
				double value = materialOption.getValue(amountToEnchant) * enchantmentOption.getValue(enchantmentLevel) * reward_factor;
				Reward reward = generateReward(QuestType.ENCHANT_ITEM, value, questPlayer);

				return new EnchantItemQuest(itemToEnchant, enchantment, enchantmentLevel, amountToEnchant, reward);
			}
		}

		// No Enchantment requirements (p.E. Shield) or no enchantment found by key
		double value = reward_factor * materialOption.getValue(amountToEnchant);
		Reward reward = generateReward(QuestType.ENCHANT_ITEM, value, questPlayer);

		return new EnchantItemQuest(itemToEnchant, amountToEnchant, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Reach Level Quest
	// ---------------------------------------------------------------------------------------

	Quest generateReachLevelQuest(QuestPlayer questPlayer, double reward_factor) throws QuestGenerationException {
		GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.REACH_LEVEL);

		int minReach = generationConfig.getDefault_min();
		int maxReach = generationConfig.getDefault_max();
		int stepReach = generationConfig.getDefault_step();
		double value_per_xp = generationConfig.getValue_per_unit();

		int playerLevel = questPlayer.getPlayer().getLevel();

		if (playerLevel > minReach - 2) { minReach = playerLevel + 2; } // Raise minimum level to reach if player.level is already higher

		if (minReach < maxReach) {

			int amountToReach = generateAmount(minReach, maxReach, stepReach, 1.0);

			double xpRequired = xpToReachLevel(amountToReach) - xpToReachLevel(playerLevel);

			double value = reward_factor * value_per_xp * xpRequired;
			Reward reward = generateReward(QuestType.REACH_LEVEL, value, questPlayer);

			return new ReachLevelQuest(questPlayer, amountToReach, reward);

		} else { // Generate a new Quest if player.level is higher than maximum level to reach
			return generate(questPlayer);
		}
	}


	// ---------------------------------------------------------------------------------------
	// Gain Level Quest
	// ---------------------------------------------------------------------------------------

	Quest generateGainLevelQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) {
		GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.GAIN_LEVEL);

		int minGain = generationConfig.getDefault_min();
		int maxGain = generationConfig.getDefault_max();
		int stepGain = generationConfig.getDefault_step();
		double value_per_level = generationConfig.getValue_per_unit();

		int amountToGain = generateAmount(minGain, maxGain, stepGain, amount_factor);

		double value = reward_factor * value_per_level * amountToGain;
		Reward reward = generateReward(QuestType.GAIN_LEVEL, value, questPlayer);

		return new GainLevelQuest(amountToGain, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Find Structure Quest
	// ---------------------------------------------------------------------------------------

	Quest generateFindStructureQuest(QuestPlayer questPlayer, double reward_factor) throws QuestGenerationException {
		GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.FIND_STRUCTURE);

		assert generationConfig.getOptions() != null;
		GenerationOption structureOption = decide(generationConfig.getOptions(), questPlayer);
		QuestStructureType structureToFind = QuestStructureType.fromString(structureOption.getName());

		// Check if Material was found
		if (structureToFind == null) {
			Main.log(Level.SEVERE,String.format("QuestStructureType '%s' does not exist.", structureOption.getName()));
			return generate(questPlayer);
		}

		// TODO: Adjust reward_factor if player has job

		double value = reward_factor * structureOption.getValue(1);
		Reward reward = generateReward(QuestType.FIND_STRUCTURE, value, questPlayer);

		return new FindStructureQuest(structureToFind, structureOption.getRadius(), 1, reward);
	}



	// ---------------------------------------------------------------------------------------
	// Helper methods
	// ---------------------------------------------------------------------------------------

	/**
	 * Returns an appropriate amount for a given DecisionObject. p.e: The amount of coal ores to break in a quest
	 * @param generationOption the GenerationOption to generate an amount for
	 * @param generationConfig the GenerationConfig that contains default values for min and max amount values
	 * @param multiplier the multiplier for the amount
	 * @return the generated amount
	 */
	public int generateAmount(GenerationOption generationOption, GenerationConfig generationConfig, double multiplier) {

		int min, max, step;

		if (generationOption.getMax() > 0 && generationOption.getStep() > 0) {
			min = generationOption.getMin();
			max = generationOption.getMax();
			step = generationOption.getStep();
		} else {
			min = generationConfig.getDefault_min();
			max = generationConfig.getDefault_max();
			step = generationConfig.getDefault_step();
		}

		if (max <= 1 || step < 1) { return 1; }

		return generateAmount(min, max, step, multiplier);
	}

	/**
	 * calculates a random amount based on the given values
	 * @param min minimum amount
	 * @param max maximum amount
	 * @param step the number the amount should be dividable by
	 * @param multiplier multiplier for the amount
	 * @return the generated amount
	 */
	public int generateAmount(int min, int max, int step, double multiplier) {
		Random r = new Random();
		double value = r.nextInt(max - min) + min;
		value = (value * multiplier) - (value * multiplier) % step;
		if (value < min) { value += step; }
		return (int) value;
	}

	/**
	 * calculates the amount factor by a players playtime based on values in config
	 * @param player the player to calculate the factor for
	 * @return the factor based on the players playtime
	 */
	protected double getPlaytimeAmountFactor(Player player) {
		FileConfiguration config = Main.getPlugin().getConfig();
		
		int ticks_played = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
		int hours_played = ticks_played / 20 / 60 / 60;
		
		double start_factor = config.getDouble("start-factor");
		double max_factor = config.getDouble("max-factor");
		double max_amount_hours = config.getDouble("max-amount-hours");
		
		return start_factor + (max_factor - start_factor) * ((double) hours_played / max_amount_hours);
	}
	
	private double xpToReachLevel(int lvl) {
		if (lvl <= 15) {
			return Math.pow(lvl, 2) + 6 * lvl;
		} else if (lvl <= 31) {
			return 2.5 * Math.pow(lvl, 2) - 40.5 * lvl + 360;
		} else {
			return 4.5 * Math.pow(lvl, 2) - 162.5 * lvl + 2220;
		}
	}

	private Reward generateReward(QuestType questType, double questValue, QuestPlayer questPlayer) {

		Random r = new Random();
		List<RewardType> list = new ArrayList<>();

		if (Config.moneyRewards() && Main.getEconomy() != null)
			list.add(RewardType.MONEY);

		if (Config.xpRewards())
			list.add(RewardType.XP);

		if (Config.itemRewards() || list.isEmpty())
			list.add(RewardType.ITEM);

		switch (list.get(r.nextInt(list.size()))) {
			case ITEM:
				List<String> materialsInRewards = new ArrayList<>();
				questPlayer.getQuests().stream().map(Quest::getReward).filter(x -> x.getMaterialNames() != null).forEach(x -> materialsInRewards.addAll(x.getMaterialNames()));
				return ItemRewardGenerator.generate(questType, questValue, materialsInRewards);

			case MONEY:
				return new Reward(new BigDecimal(Math.round(questValue * Config.getMoneyFactor())));

			case XP:
				return new Reward((int) (questValue * 0.6));
		}

		return new Reward();
	}
}

