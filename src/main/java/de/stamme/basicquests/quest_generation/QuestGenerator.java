package de.stamme.basicquests.quest_generation;

import de.stamme.basicquests.data.Config;
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
import java.util.Map;
import java.util.Random;

public class QuestGenerator {
	
	// Main Method only for Testing Purpose
	public static void main(String[] args) {


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
	}
	

	private static final String quest_types_path = "/quest_generation/quest_types.json";
	private static final String break_block_path = "/quest_generation/break_block.json";
	private static final String mine_block_path = "/quest_generation/mine_block.json";
	private static final String harvest_block_path = "/quest_generation/harvest_block.json";
	private static final String kill_entity_path = "/quest_generation/kill_entity.json";
	private static final String enchant_item_path = "/quest_generation/enchant_item.json";
	private static final String gain_level_path = "/quest_generation/gain_level.json";
	private static final String reach_level_path = "/quest_generation/reach_level.json";
	private static final String find_structure_path = "/quest_generation/find_structure.json";
	
    
	// randomly decides for an object based on the given weight
	public static DecisionObject decide(ArrayList<DecisionObject> objects) {
		double x = 0;
		double tot = objects.stream()
				.map(y -> y.weight)
				.mapToDouble(Double::doubleValue)
				.sum();
		
		Random r = new Random();
		double target = tot * r.nextDouble();
		
		for (DecisionObject obj: objects) {
			x += obj.weight;
			if (x >= target) { return obj; }
		}
		
		return null;
	}
	
	public static DecisionObject decide(ArrayList<DecisionObject> objects, QuestPlayer player) {
//		boolean consider_jobs = Config.getConsiderJobs();
//		double job_weight_factor = Config.getWeightFactor();
		
		for (DecisionObject obj: objects) {
			
			// set DecisionObjects weight to 0 if a required advancement has not been made (eg. "story/mine_diamond")
			if (obj.advancements != null) {
				for (String key: obj.advancements) {
					key = key.replace(".", "/");
					Advancement adv = Bukkit.getAdvancement(NamespacedKey.minecraft(key));
					if (adv != null && !player.player.getAdvancementProgress(adv).isDone()) {
						obj.weight = 0;
					}
				}
			}
			
			// Reduce weight of Decision Objects that are already in the players quests
			if (player.quests != null) {
				for (Quest quest: player.quests) {
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
	
	public static Quest generate(QuestPlayer player) throws QuestGenerationException {
		Random r = new Random();

		double reward_factor = Config.getRewardFactor();
		double amount_factor = Config.getQuantityFactor();

		if (Config.increaseAmountByPlaytime())
			amount_factor *= getPlaytimeAmountFactor(player.player);
		
		
		
		Quest quest = null;
		ArrayList<DecisionObject> questTypesDOs = JsonManager.getDecisionObjects(quest_types_path);
		DecisionObject questTypeDO = decide(questTypesDOs, player);

		QuestType questType;

		try {
			questType = QuestType.valueOf(questTypeDO.name);

		} catch(IllegalArgumentException exception) {
			// If QuestType was not found
			throw new QuestGenerationException(String.format("QuestType '%s' does not exist.", questTypeDO.name));
		}

		
		
		
		// BREAK BLOCK ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		if (questType == QuestType.BREAK_BLOCK) {
			
			Map<String, Object> breakBlockJsonMap = JsonManager.read(break_block_path);
			assert breakBlockJsonMap != null;

			ArrayList<DecisionObject> materialsToBreakDOs = JsonManager.getDecisionObjects(breakBlockJsonMap);
			DecisionObject materialToBreakDO = QuestGenerator.decide(materialsToBreakDOs, player);
			
			Material materialToBreak = null;
			if (!materialToBreakDO.name.equalsIgnoreCase("LOG")) {
				materialToBreak = Material.valueOf(materialToBreakDO.name);
			}
			
			// Check if Material was found   OR   material.name == LOG
			if (materialToBreak == null && !materialToBreakDO.name.equalsIgnoreCase("LOG")) { throw new QuestGenerationException(String.format("Material '%s' does not exist.", materialToBreakDO.name)); }
			
			// TODO: Adjust amount_factor if player has job
			// TODO: Adjust reward_factor if player has job
			
			int amountToBreak = generateAmount(materialToBreakDO, breakBlockJsonMap, amount_factor);
			
			double value = questTypeDO.value * materialToBreakDO.value * amountToBreak * reward_factor;
			Reward reward = generateReward(questType, value, player);
			
			
			if (materialToBreakDO.name.equalsIgnoreCase("LOG")) { // General Log quest that accepts all kind of logs
				quest = new BlockBreakQuest(materialToBreakDO.name, amountToBreak, reward);
				
			} else {
				quest = new BlockBreakQuest(materialToBreak, amountToBreak, reward);
			}
			
		}
		
		
		// MINE_BLOCK ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		else if (questType == QuestType.MINE_BLOCK) {
			
			Map<String, Object> mineBlockJsonMap = JsonManager.read(mine_block_path);
			assert mineBlockJsonMap != null;

			ArrayList<DecisionObject> materialsToMineDOs = JsonManager.getDecisionObjects(mineBlockJsonMap);
			DecisionObject materialToMineDO = QuestGenerator.decide(materialsToMineDOs, player);
			Material materialToMine;

			try {
				materialToMine = Material.valueOf(materialToMineDO.name);
			} catch(IllegalArgumentException exception) {
				// If Material was not found
				throw new QuestGenerationException(String.format("Material '%s' does not exist.", materialToMineDO.name));
			}
			
			// TODO: Adjust amount_factor if player has job
			// TODO: Adjust reward_factor if player has job
			
			int amountToMine = generateAmount(materialToMineDO, mineBlockJsonMap, amount_factor);
			
			double value = questTypeDO.value * materialToMineDO.value * amountToMine * reward_factor;
			Reward reward = generateReward(questType, value, player);
			
			quest = new MineBlockQuest(materialToMine, amountToMine, reward);
		}
		
		
		// HARVEST_BLOCK +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		else if (questType == QuestType.HARVEST_BLOCK) {
			
			Map<String, Object> harvestBlockJsonMap = JsonManager.read(harvest_block_path);
			assert harvestBlockJsonMap != null;

			ArrayList<DecisionObject> materialsToHarvestDOs = JsonManager.getDecisionObjects(harvestBlockJsonMap);
			DecisionObject materialToHarvestDO = QuestGenerator.decide(materialsToHarvestDOs, player);
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
			
			double value = questTypeDO.value * materialToHarvestDO.value * amountToHarvest * reward_factor;
			Reward reward = generateReward(questType, value, player);
			
			quest = new HarvestBlockQuest(materialToHarvest, amountToHarvest, reward);
		}
		
		
		// KILL_ENTITY +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		else if (questType == QuestType.KILL_ENTITY) {
			
			Map<String, Object> killEntityJsonMap = JsonManager.read(kill_entity_path);
			assert killEntityJsonMap != null;

			ArrayList<DecisionObject> entitiesToKillDOs = JsonManager.getDecisionObjects(killEntityJsonMap);
			DecisionObject entityToKillDO = decide(entitiesToKillDOs, player);
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
			
			double value = questTypeDO.value * entityToKillDO.value * amountToKill * reward_factor;
			Reward reward = generateReward(questType, value, player);
			
			quest = new EntityKillQuest(entityToKill, amountToKill, reward);
						
		}
		
		
		// ENCHANT_ITEM ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		else if (questType == QuestType.ENCHANT_ITEM) {
			
			Map<String, Object> enchantItemJsonMap = JsonManager.read(enchant_item_path);
			assert enchantItemJsonMap != null;

			ArrayList<DecisionObject> itemsToEnchantDOs = JsonManager.getDecisionObjects(enchantItemJsonMap);
			DecisionObject itemToEnchantDO = decide(itemsToEnchantDOs, player);
			Material itemToEnchant;

			try {
				itemToEnchant = Material.valueOf(itemToEnchantDO.name);
			} catch(IllegalArgumentException exception) {
				// If Material was not found
				throw new QuestGenerationException(String.format("Material '%s' does not exist.", itemToEnchantDO.name));
			}

			// TODO: Adjust amount_factor if player has job
			// TODO: Adjust reward_factor if player has job
			
			int amountToEnchant = generateAmount(itemToEnchantDO, enchantItemJsonMap, amount_factor);
			
			
			if (itemToEnchantDO.decisionObjects != null) { // When Enchantments are available					
				
				DecisionObject enchantmentDO = decide(itemToEnchantDO.decisionObjects);
				assert enchantmentDO != null;

				Enchantment enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantmentDO.name.toLowerCase()));
				
				if (enchantment != null) {
					int enchantmentLevel = 1;
					if (enchantment.getMaxLevel() > 2) {
						enchantmentLevel = r.nextInt(enchantment.getMaxLevel() - 1) + 1;
					}
					double value = questTypeDO.value * itemToEnchantDO.value * enchantmentDO.value * enchantmentLevel * reward_factor * amountToEnchant;
					Reward reward = generateReward(questType, value, player);
					
					quest = new EnchantItemQuest(itemToEnchant, enchantment, enchantmentLevel, amountToEnchant, reward);
				}
			}
			
			if (quest == null) { // No Enchantment requirements (p.E. Shield) or no enchantment found by key
				double value = questTypeDO.value * itemToEnchantDO.value * reward_factor * amountToEnchant;
				Reward reward = generateReward(questType, value, player);
				
				quest = new EnchantItemQuest(itemToEnchant, amountToEnchant, reward);
			}

			
		}
		
		
		// REACH_LEVEL +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		else if (questType == QuestType.REACH_LEVEL) {
			
			Map<String, Object> reachLevelJsonMap = JsonManager.read(reach_level_path);
			assert reachLevelJsonMap != null;

			int minReach = (reachLevelJsonMap.get("min") instanceof Double) ? (int) (double) reachLevelJsonMap.get("min") : 20;
			int maxReach = (reachLevelJsonMap.get("max") instanceof Double) ? (int) (double) reachLevelJsonMap.get("max") : 60;
			int stepReach = (reachLevelJsonMap.get("step") instanceof Double) ? (int) (double) reachLevelJsonMap.get("step") : 5;
			double value_per_xp = (reachLevelJsonMap.get("value") instanceof Double) ? (double) reachLevelJsonMap.get("value") : 1;
			
			int playerLevel = player.player.getLevel();
			
			if (playerLevel > minReach - 2) { minReach = playerLevel + 2; } // Raise minimum level to reach if player.level is already higher
			
			if (minReach < maxReach) {
			
				int amountToReach = generateAmount(minReach, maxReach, stepReach, 1.0);
				
				double xpRequired = xpToReachLevel(amountToReach) - xpToReachLevel(playerLevel);
				
				double value = questTypeDO.value * value_per_xp * reward_factor * xpRequired;
				Reward reward = generateReward(questType, value, player);
				
				quest = new ReachLevelQuest(player, amountToReach, reward);
				
			} else { // Generate a new Quest if player.level is higher than maximum level to reach
				quest = generate(player);
			}
			
		}
		
		
		// GAIN_LEVEL ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		else if (questType == QuestType.GAIN_LEVEL) {
			
			Map<String, Object> gainLevelJsonMap = JsonManager.read(gain_level_path);
			assert gainLevelJsonMap != null;

			int minGain = (gainLevelJsonMap.get("min") instanceof Double) ? (int) (double) gainLevelJsonMap.get("min") : 10;
			int maxGain = (gainLevelJsonMap.get("max") instanceof Double) ? (int) (double) gainLevelJsonMap.get("max") : 50;
			int stepGain = (gainLevelJsonMap.get("step") instanceof Double) ? (int) (double) gainLevelJsonMap.get("step") : 5;
			double value_per_level = (gainLevelJsonMap.get("value") instanceof Double) ? (double) gainLevelJsonMap.get("value") : 20.0;
			
			int amountToGain = generateAmount(minGain, maxGain, stepGain, amount_factor);
			
			double value = questTypeDO.value * value_per_level * reward_factor * amountToGain;
			Reward reward = generateReward(questType, value, player);
			
			quest = new GainLevelQuest(amountToGain, reward);
			
		}
		
		
		// FIND_STRUCTURE +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		else if (questType == QuestType.FIND_STRUCTURE) {
			
			Map<String, Object> findStructureJsonMap = JsonManager.read(find_structure_path);
			assert findStructureJsonMap != null;

			ArrayList<DecisionObject> structuresToFindDOs = JsonManager.getDecisionObjects(findStructureJsonMap);
			DecisionObject structureToFindDO = decide(structuresToFindDOs, player);
			StructureType structureToFind = StructureType.getStructureTypes().get(structureToFindDO.name.toLowerCase());
			
			// Check if Material was found
			if (structureToFind == null) { throw new QuestGenerationException(String.format("Structure '%s' does not exist.", structureToFindDO.name)); }
			
			// TODO: Adjust reward_factor if player has job
			
			double value = questTypeDO.value * structureToFindDO.value * reward_factor;
			Reward reward = generateReward(questType, value, player);
			
			quest = new FindStructureQuest(structureToFind, structureToFindDO.radius, 1, reward);
			
		}
		
		
		
		return quest;
	}
	
	
	// Returns an appropriate amount for a given DecisionObject. p.e: The amount of coal ores to break in a quest
	protected static int generateAmount(DecisionObject obj, Map<String, Object> jsonMap, double multiplier) {
		
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
	
	// calculates a random amount based on the given values
	protected static int generateAmount(int min, int max, int step, double multiplier) {
		Random r = new Random();
		double value = r.nextInt(max - min) + min;
		value = (value * multiplier) - (value * multiplier) % step;
		if (value < min) { value += step; }
		return (int) value;
	}
	
	// calculates the amount factor by a players playtime based on values in config
	protected static double getPlaytimeAmountFactor(Player player) {
		FileConfiguration config = Main.plugin.getConfig();
		
		int ticks_played = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
		int hours_played = ticks_played / 20 / 60 / 60;
		
		double start_factor = config.getDouble("start-factor");
		double max_factor = config.getDouble("max-factor");
		double max_amount_hours = config.getDouble("max-amount-hours");
		
		return start_factor + (max_factor - start_factor) * ((double) hours_played / max_amount_hours);
	}
	
	private static double xpToReachLevel(int lvl) {
		if (lvl <= 15) {
			return Math.pow(lvl, 2) + 6 * lvl;
		} else if (lvl <= 31) {
			return 2.5 * Math.pow(lvl, 2) - 40.5 * lvl + 360;
		} else {
			return 4.5 * Math.pow(lvl, 2) - 162.5 * lvl + 2220;
		}
	}

	private static Reward generateReward(QuestType questType, double questValue, QuestPlayer player) {

		Random r = new Random();
		ArrayList<RewardType> list = new ArrayList<>();

		if (Config.moneyRewards() && Main.getEconomy() != null)
			list.add(RewardType.MONEY);

		if (Config.xpRewards())
			list.add(RewardType.XP);

		if (Config.itemRewards() || list.isEmpty())
			list.add(RewardType.ITEM);

		switch (list.get(r.nextInt(list.size()))) {
			case ITEM:
				ArrayList<String> materialsInRewards = new ArrayList<>();
				player.quests.stream().map(x->x.reward).filter(x->x.materialNames != null).forEach(x->materialsInRewards.addAll(x.materialNames));
				return ItemRewardGenerator.generate(questType, questValue, materialsInRewards);

			case MONEY:
				return new Reward(new BigDecimal(Math.round(questValue * Config.getMoneyFactor())));

			case XP:
				return new Reward((int) (questValue * 0.6));
		}

		return new Reward();
	}
}

