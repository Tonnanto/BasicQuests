package de.stamme.basicquests.quests;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import de.stamme.basicquests.main.StringFormatter;

public class EnchantItemQuest extends Quest {
	
	public Material material;
	public Enchantment enchantment;
	public int lvl = 0;

	// If lvl does not matter -> lvl = 0
	public EnchantItemQuest(Material mat, Enchantment enchantment, int lvl, int goal, Reward reward) {
		super(goal, reward);
		this.material = mat;
		this.enchantment = enchantment;
		this.lvl = lvl;
	}
	
	// Constructor without enchantment requirement
	public EnchantItemQuest(Material mat, int goal, Reward reward) {
		super(goal, reward);
		this.material = mat;
	}
	
	public QuestData toData() {
		QuestData data = super.toData();
		
		data.questType = QuestType.ENCHANT_ITEM.name();
		data.material = material.name();
		
		if (enchantment != null) {
			data.enchantment = enchantment.getKey().toString().split(":")[1];
			data.enchantmentLvl = lvl;
		}
		
		return data;
	}
	
	
	// Returns a String in the format: "Enchant <amount> <material> with <enchantment> <lvl>"
	public String getName() {
		
		String mat_name = StringFormatter.format(material.toString());
		
		// no enchantment requirement
		if (enchantment == null) {
			return String.format("Enchant %s %s%s", (goal == 1) ? "a" : goal, mat_name, (goal > 1) ? "s" : "");
			
		// with enchantment requirement
		} else {
			String lvlString = StringFormatter.enchantmentLevel(enchantment, lvl);
			return String.format("Enchant %s %s%s with %s %s", (goal == 1) ? "a" : goal, mat_name, (goal > 1) ? "s" : "", StringFormatter.enchantmentName(enchantment), (lvlString.length() > 0) ? lvlString + "+" : "");
		}
		
	}
	
	public String[] getDecisionObjectNames() {
		String enchantmentStr = (enchantment != null) ? enchantment.toString() : "";
		return new String[]{QuestType.ENCHANT_ITEM.name(), material.name(), enchantmentStr};
	}
	
}
