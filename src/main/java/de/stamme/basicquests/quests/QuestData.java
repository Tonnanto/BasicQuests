package de.stamme.basicquests.quests;

import de.stamme.basicquests.main.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.StructureType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityType;

import java.io.Serializable;

public class QuestData implements Serializable {
	private static final long serialVersionUID = -3976762424091379760L;
	
	// ALL QUESTS
	String questType;
	int goal;
	int count;
	Reward reward;
	boolean rewardReceived;
	
	// BREAK_BLOCK - HARVEST_BLOCK - ENCHANT_ITEM
	String material;
	
	// BREAK_BLOCK (Log)
	String materialString;
	
	// KILL_ENTITY
	String entity;
	
	// ENCHANT_ITEM
	String enchantment;
	int enchantmentLvl;
	
	// FIND_STRUCTURE
	String structure;
	double radius;
	
	
	// Tries to initialize a Quest based on its attributes
	public Quest toQuest() {
		
		Quest quest = null;
		
		if (questType.equals(QuestType.BREAK_BLOCK.name())) {

			if (materialString != null && materialString.equalsIgnoreCase("LOG")) {
				quest = new BlockBreakQuest("LOG", goal, reward);
						
			} else {
				try {
					Material mat = Material.valueOf(material);
					quest = new BlockBreakQuest(mat, goal, reward);

				} catch (Exception exception) {
					Main.log(String.format("Material '%s' does not exist.", material));
				}
			}
			
			
		} else if (questType.equals(QuestType.MINE_BLOCK.name())) {
			try {
				Material mat = Material.valueOf(material);
				quest = new MineBlockQuest(mat, goal, reward);

			} catch (Exception exception) {
				Main.log(String.format("Material '%s' does not exist.", material));
			}
			
			
		} else if (questType.equals(QuestType.HARVEST_BLOCK.name())) {
			try {
				Material mat = Material.valueOf(material);
				quest = new HarvestBlockQuest(mat, goal, reward);

			} catch (Exception exception) {
				Main.log(String.format("Material '%s' does not exist.", material));
			}
			
			
		} else if (questType.equals(QuestType.ENCHANT_ITEM.name())) {
			Enchantment enc = null;
			if (enchantment != null ) {
				enc = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantment.toLowerCase()));
			}
			
			try {
				Material mat = Material.valueOf(material);
				if (enc != null) {
					quest = new EnchantItemQuest(mat, enc, enchantmentLvl, goal, reward);
				} else {
					quest = new EnchantItemQuest(mat, goal, reward);
				}
			} catch (Exception exception) {
				Main.log(String.format("Material '%s' does not exist.", material));
			}
			
			
		} else if (questType.equals(QuestType.KILL_ENTITY.name())) {
			try {
				EntityType ent = EntityType.valueOf(entity);
				quest = new EntityKillQuest(ent, goal, reward);

			} catch (Exception exception) {
				Main.log(String.format("EntityType '%s' does not exist.", entity));
			}
			
			
		} else if (questType.equals(QuestType.GAIN_LEVEL.name())) {
			quest = new GainLevelQuest(goal, reward);
			
			
		} else if (questType.equals(QuestType.REACH_LEVEL.name())) {
			quest = new ReachLevelQuest(goal, reward);
			
			
		} else if (questType.equals(QuestType.FIND_STRUCTURE.name())) {
			StructureType str = StructureType.getStructureTypes().get(structure);
			if (str != null) {
				quest = new FindStructureQuest(str, radius, goal, reward);
			}
			
		}
		
		
		
		
		// if quest was successfully initialized -> adjust count
		if (quest != null) {
			quest.count = count;
		}
		
		return quest;
	}
	
	
	public String toString() {
		return String.format("Type: %s, goal: %s, count %s, reward %s, mat: %s, ent: %s, enc: %s", questType, goal, count, reward.money, material, entity, enchantment);
	}

	public String getQuestType() {
		return questType;
	}

	public Reward getReward() {
		return reward;
	}

}
