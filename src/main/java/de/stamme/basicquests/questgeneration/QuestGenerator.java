package de.stamme.basicquests.questgeneration;

import de.stamme.basicquests.data.Config;
import de.stamme.basicquests.data.GenerationFileService;
import de.stamme.basicquests.data.JsonManager;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.*;
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
import java.util.Map;
import java.util.Random;

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
	

//	private final String quest_types_path = "/quest_generation/quest_types.json";
	private final String break_block_path = "/quest_generation/break_block.json";
//	private final String mine_block_path = "/quest_generation/mine_block.json";
//	private final String chop_wood_path = "/quest_generation/chop_wood.json";
	private final String harvest_block_path = "/quest_generation/harvest_block.json";
	private final String kill_entity_path = "/quest_generation/kill_entity.json";
	private final String enchant_item_path = "/quest_generation/enchant_item.json";
	private final String gain_level_path = "/quest_generation/gain_level.json";
	private final String reach_level_path = "/quest_generation/reach_level.json";
	private final String find_structure_path = "/quest_generation/find_structure.json";

	/**
	 * randomly decides for an object based on the given weight
	 * @param objects objects to decide from
	 * @return the decided object
	 */
	public GenerationOption decide(List<GenerationOption> objects) {
		double x = 0;
		double tot = objects.stream()
				.map(y -> y.weight)
				.mapToDouble(Double::doubleValue)
				.sum();
		
		Random r = new Random();
		double target = tot * r.nextDouble();
		
		for (GenerationOption obj: objects) {
			x += obj.weight;
			if (x >= target) { return obj; }
		}
		
		return null;
	}
	
	public GenerationOption decide(List<GenerationOption> objects, QuestPlayer questPlayer) {
//		boolean consider_jobs = Config.getConsiderJobs();
//		double job_weight_factor = Config.getWeightFactor();
		
		for (GenerationOption obj: objects) {
			
			// set DecisionObjects weight to 0 if a required advancement has not been made (eg. "story/mine_diamond")
			if (obj.advancements != null) {
				for (String key: obj.advancements) {
					key = key.replace(".", "/");
					Advancement adv = Bukkit.getAdvancement(NamespacedKey.minecraft(key));
					if (adv != null && !questPlayer.getPlayer().getAdvancementProgress(adv).isDone()) {
						obj.weight = 0;
					}
				}
			}
			
			// Reduce weight of Decision Objects that are already in the players quests
			if (questPlayer.getQuests() != null) {
				for (Quest quest: questPlayer.getQuests()) {
					for (String name: quest.getDecisionObjectNames()) {
						if (obj.name.equalsIgnoreCase(name)) { obj.weight *= Config.duplicateQuestChance(); break; }
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

		GenerationOption questTypeOption = decide(generationConfig.getOptions(), questPlayer);

		QuestType questType;

		try {
			questType = QuestType.valueOf(questTypeOption.name);
		} catch(IllegalArgumentException exception) {
			// QuestType was not found
			throw new QuestGenerationException(String.format("QuestType '%s' does not exist.", questTypeOption.name));
		}

		switch (questType) {
			case BREAK_BLOCK:
				quest = generateBreakBlockQuest(questPlayer, questTypeOption.value * reward_factor, amount_factor);
				break;
			case MINE_BLOCK:
				quest = generateMineBlockQuest(questPlayer, questTypeOption.value * reward_factor, amount_factor);
				break;
			case HARVEST_BLOCK:
				quest = generateHarvestBlockQuest(questPlayer, questTypeOption.value * reward_factor, amount_factor);
				break;
			case ENCHANT_ITEM:
				quest = generateEnchantItemQuest(questPlayer, questTypeOption.value * reward_factor, amount_factor);
				break;
			case KILL_ENTITY:
				quest = generateKillEntityQuest(questPlayer, questTypeOption.value * reward_factor, amount_factor);
				break;
			case GAIN_LEVEL:
				quest = generateGainLevelQuest(questPlayer, questTypeOption.value * reward_factor, amount_factor);
				break;
			case REACH_LEVEL:
				quest = generateReachLevelQuest(questPlayer, questTypeOption.value * reward_factor);
				break;
			case FIND_STRUCTURE:
				quest = generateFindStructureQuest(questPlayer, questTypeOption.value * reward_factor);
				break;
			case CHOP_WOOD:
				quest = generateChopWoodQuest(questPlayer, questTypeOption.value * reward_factor, amount_factor);
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
		Map<String, Object> breakBlockJsonMap = JsonManager.read(break_block_path);
		assert breakBlockJsonMap != null;

		List<GenerationOption> materialsToBreakDOs = JsonManager.getDecisionObjects(breakBlockJsonMap);
		GenerationOption materialToBreakDO = decide(materialsToBreakDOs, questPlayer);
		Material materialToBreak;

		try {
			materialToBreak = Material.valueOf(materialToBreakDO.name);
		} catch(IllegalArgumentException exception) {
			// If Material was not found
			throw new QuestGenerationException(String.format("Material '%s' does not exist.", materialToBreakDO.name));
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToBreak = generateAmount(materialToBreakDO, breakBlockJsonMap, amount_factor);

		double value = materialToBreakDO.value * amountToBreak * reward_factor;
		Reward reward = generateReward(QuestType.BREAK_BLOCK, value, questPlayer);

		return new BlockBreakQuest(materialToBreak, amountToBreak, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Mine Block Quest
	// ---------------------------------------------------------------------------------------

	Quest generateMineBlockQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
//		Map<String, Object> mineBlockJsonMap = JsonManager.read(mine_block_path);
//		assert mineBlockJsonMap != null;

		GenerationConfig generationConfig = GenerationFileService.getInstance().getMineBlockGenerationConfig();
		GenerationOption materialToMineDO = decide(generationConfig.getOptions(), questPlayer);
		Material materialToMine;

		try {
			materialToMine = Material.valueOf(materialToMineDO.name);
		} catch(IllegalArgumentException exception) {
			// If Material was not found
			throw new QuestGenerationException(String.format("Material '%s' does not exist.", materialToMineDO.name));
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToMine = generateAmount(materialToMineDO, generationConfig, amount_factor);

		double value = (materialToMineDO.value_base + materialToMineDO.value_per_unit * amountToMine) * reward_factor;
		Reward reward = generateReward(QuestType.MINE_BLOCK, value, questPlayer);

		return new MineBlockQuest(materialToMine, amountToMine, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Harvest Block Quest
	// ---------------------------------------------------------------------------------------

	Quest generateHarvestBlockQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {

		Map<String, Object> harvestBlockJsonMap = JsonManager.read(harvest_block_path);
		assert harvestBlockJsonMap != null;

		List<GenerationOption> materialsToHarvestDOs = JsonManager.getDecisionObjects(harvestBlockJsonMap);
		GenerationOption materialToHarvestDO = decide(materialsToHarvestDOs, questPlayer);
		Material materialToHarvest;

		try {
			materialToHarvest = Material.valueOf(materialToHarvestDO.name);
		} catch(IllegalArgumentException exception) {
			// If Material was not found
			throw new QuestGenerationException(String.format("Material '%s' does not exist.", materialToHarvestDO.name));
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToHarvest = generateAmount(materialToHarvestDO, harvestBlockJsonMap, amount_factor);

		double value = materialToHarvestDO.value * amountToHarvest * reward_factor;
		Reward reward = generateReward(QuestType.HARVEST_BLOCK, value, questPlayer);

		return new HarvestBlockQuest(materialToHarvest, amountToHarvest, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Chop Wood Quest
	// ---------------------------------------------------------------------------------------

	Quest generateChopWoodQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
//		Map<String, Object> chopWoodJsonMap = JsonManager.read(chop_wood_path);
//		assert chopWoodJsonMap != null;

		GenerationConfig generationConfig = GenerationFileService.getInstance().getChopWoodGenerationConfig();
		GenerationOption woodOption = decide(generationConfig.getOptions(), questPlayer);
		Material woodToChop = null;

		if (!woodOption.name.equalsIgnoreCase("LOG")) {
			try {
				woodToChop = Material.valueOf(woodOption.name);
			} catch(IllegalArgumentException exception) {
				// If Material was not found
				throw new QuestGenerationException(String.format("Material '%s' does not exist.", woodOption.name));
			}
		}

		// Check if Material was found   OR   material.name == LOG
		if (woodToChop == null && !woodOption.name.equalsIgnoreCase("LOG")) {
			throw new QuestGenerationException(String.format("Material '%s' does not exist.", woodOption.name));
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToChop = generateAmount(woodOption, generationConfig, amount_factor);
		double value = (woodOption.value_base + woodOption.value_per_unit * amountToChop) * reward_factor;
		Reward reward = generateReward(QuestType.CHOP_WOOD, value, questPlayer);

		if (woodOption.name.equalsIgnoreCase("LOG")) { // General Log quest that accepts all kind of logs
			return new ChopWoodQuest(woodOption.name, amountToChop, reward);
		} else {
			return new ChopWoodQuest(woodToChop, amountToChop, reward);
		}
	}


	// ---------------------------------------------------------------------------------------
	// Kill Entity Quest
	// ---------------------------------------------------------------------------------------

	Quest generateKillEntityQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {

		Map<String, Object> killEntityJsonMap = JsonManager.read(kill_entity_path);
		assert killEntityJsonMap != null;

		List<GenerationOption> entitiesToKillDOs = JsonManager.getDecisionObjects(killEntityJsonMap);
		GenerationOption entityToKillDO = decide(entitiesToKillDOs, questPlayer);
		EntityType entityToKill;

		try {
			entityToKill =  EntityType.valueOf(entityToKillDO.name);
		} catch(IllegalArgumentException exception) {
			// If Entity was not found
			throw new QuestGenerationException(String.format("Entity '%s' does not exist.", entityToKillDO.name));
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToKill = generateAmount(entityToKillDO, killEntityJsonMap, amount_factor);

		double value = entityToKillDO.value * amountToKill * reward_factor;
		Reward reward = generateReward(QuestType.KILL_ENTITY, value, questPlayer);

		return new EntityKillQuest(entityToKill, amountToKill, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Enchant Item Quest
	// ---------------------------------------------------------------------------------------

	Quest generateEnchantItemQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {

		Map<String, Object> enchantItemJsonMap = JsonManager.read(enchant_item_path);
		assert enchantItemJsonMap != null;

		List<GenerationOption> itemsToEnchantDOs = JsonManager.getDecisionObjects(enchantItemJsonMap);
		GenerationOption itemToEnchantDO = decide(itemsToEnchantDOs, questPlayer);
		Material itemToEnchant;

		try {
			itemToEnchant = Material.valueOf(itemToEnchantDO.name);
		} catch (IllegalArgumentException exception) {
			// If Material was not found
			throw new QuestGenerationException(String.format("Material '%s' does not exist.", itemToEnchantDO.name));
		}

		// TODO: Adjust amount_factor if player has job
		// TODO: Adjust reward_factor if player has job

		int amountToEnchant = generateAmount(itemToEnchantDO, enchantItemJsonMap, amount_factor);


		if (itemToEnchantDO.decisionObjects != null) { // When Enchantments are available

			GenerationOption enchantmentDO = decide(itemToEnchantDO.decisionObjects);
			assert enchantmentDO != null;

			Enchantment enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantmentDO.name.toLowerCase()));

			if (enchantment != null) {
				int enchantmentLevel = 1;
				if (enchantment.getMaxLevel() > 2) {
					Random r = new Random();
					enchantmentLevel = r.nextInt(enchantment.getMaxLevel() - 1) + 1;
				}
				double value = itemToEnchantDO.value * enchantmentDO.value * enchantmentLevel * reward_factor * amountToEnchant;
				Reward reward = generateReward(QuestType.ENCHANT_ITEM, value, questPlayer);

				return new EnchantItemQuest(itemToEnchant, enchantment, enchantmentLevel, amountToEnchant, reward);
			}
		}

		// No Enchantment requirements (p.E. Shield) or no enchantment found by key
		double value = itemToEnchantDO.value * reward_factor * amountToEnchant;
		Reward reward = generateReward(QuestType.ENCHANT_ITEM, value, questPlayer);

		return new EnchantItemQuest(itemToEnchant, amountToEnchant, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Reach Level Quest
	// ---------------------------------------------------------------------------------------

	Quest generateReachLevelQuest(QuestPlayer questPlayer, double reward_factor) throws QuestGenerationException {
		Map<String, Object> reachLevelJsonMap = JsonManager.read(reach_level_path);
		assert reachLevelJsonMap != null;

		int minReach = (reachLevelJsonMap.get("min") instanceof Double) ? (int) (double) reachLevelJsonMap.get("min") : 20;
		int maxReach = (reachLevelJsonMap.get("max") instanceof Double) ? (int) (double) reachLevelJsonMap.get("max") : 60;
		int stepReach = (reachLevelJsonMap.get("step") instanceof Double) ? (int) (double) reachLevelJsonMap.get("step") : 5;
		double value_per_xp = (reachLevelJsonMap.get("value") instanceof Double) ? (double) reachLevelJsonMap.get("value") : 1;

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
		Map<String, Object> gainLevelJsonMap = JsonManager.read(gain_level_path);
		assert gainLevelJsonMap != null;

		int minGain = (gainLevelJsonMap.get("min") instanceof Double) ? (int) (double) gainLevelJsonMap.get("min") : 10;
		int maxGain = (gainLevelJsonMap.get("max") instanceof Double) ? (int) (double) gainLevelJsonMap.get("max") : 50;
		int stepGain = (gainLevelJsonMap.get("step") instanceof Double) ? (int) (double) gainLevelJsonMap.get("step") : 5;
		double value_per_level = (gainLevelJsonMap.get("value") instanceof Double) ? (double) gainLevelJsonMap.get("value") : 20.0;

		int amountToGain = generateAmount(minGain, maxGain, stepGain, amount_factor);

		double value = reward_factor * value_per_level * amountToGain;
		Reward reward = generateReward(QuestType.GAIN_LEVEL, value, questPlayer);

		return new GainLevelQuest(amountToGain, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Find Structure Quest
	// ---------------------------------------------------------------------------------------

	Quest generateFindStructureQuest(QuestPlayer questPlayer, double reward_factor) throws QuestGenerationException {
		Map<String, Object> findStructureJsonMap = JsonManager.read(find_structure_path);
		assert findStructureJsonMap != null;

		List<GenerationOption> structuresToFindDOs = JsonManager.getDecisionObjects(findStructureJsonMap);
		GenerationOption structureToFindDO = decide(structuresToFindDOs, questPlayer);
		StructureType structureToFind = StructureType.getStructureTypes().get(structureToFindDO.name.toLowerCase());

		// Check if Material was found
		if (structureToFind == null) { throw new QuestGenerationException(String.format("Structure '%s' does not exist.", structureToFindDO.name)); }

		// TODO: Adjust reward_factor if player has job

		double value = reward_factor * structureToFindDO.value;
		Reward reward = generateReward(QuestType.FIND_STRUCTURE, value, questPlayer);

		return new FindStructureQuest(structureToFind, structureToFindDO.radius, 1, reward);
	}



	// ---------------------------------------------------------------------------------------
	// Helper methods
	// ---------------------------------------------------------------------------------------

	/**
	 * Returns an appropriate amount for a given DecisionObject. p.e: The amount of coal ores to break in a quest
	 * @param obj the DecisionObject to generate an amount for
	 * @param jsonMap the map that contains the min and max amount values for this Decision Object
	 * @param multiplier the multiplier for the amount
	 * @return the generated amount
	 */
	public int generateAmount(GenerationOption obj, Map<String, Object> jsonMap, double multiplier) {
		
		int min, max, step;
		
		if (obj.max > 0 && obj.step > 0) {
			min = obj.min;
			max = obj.max;
			step = obj.step;
		} else {
			min = (jsonMap.get("default_min") instanceof Double) ? (int) (double) jsonMap.get("default_min") : 0;
			max = (jsonMap.get("default_max") instanceof Double) ? (int) (double) jsonMap.get("default_max") : 0;
			step = (jsonMap.get("default_step") instanceof Double) ? (int) (double) jsonMap.get("default_step") : 0;
		}
		
		if (max <= 1 || step < 1) { return 1; }
		
		return generateAmount(min, max, step, multiplier);
	}

	// TODO Make this the new generateAmount
	public int generateAmount(GenerationOption obj, GenerationConfig generationConfig, double multiplier) {

		int min, max, step;

		if (obj.max > 0 && obj.step > 0) {
			min = obj.min;
			max = obj.max;
			step = obj.step;
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

