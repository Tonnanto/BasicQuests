package de.stamme.basicquests.quests;

import de.stamme.basicquests.util.StringFormatter;
import org.bukkit.Material;

public class BlockBreakQuest extends Quest {

	public Material material;
	public String materialString = "";

	public BlockBreakQuest(Material mat, int goal, Reward reward) {
		super(goal, reward);
		this.material = mat;
	}
	
	// Initializer for 'Log' Quests that accept any kind of log
	public BlockBreakQuest(String mat, int goal, Reward reward) {
		super(goal, reward);
		this.material = Material.OAK_LOG; // 
		this.materialString = mat;
	}
	
	public QuestData toData() {
		QuestData data = super.toData();
		
		data.questType = QuestType.BREAK_BLOCK.name();
		data.material = material.name();
		data.materialString = materialString;
		
		return data;
	}

	// Returns a String in the format: "Break <amount> <material>"
	public String getName() {
		if (materialString != null && !materialString.isEmpty()) { return String.format("Chop %s %ss", goal, StringFormatter.format(materialString)); }
		else if (material == Material.ACACIA_LOG |
				material == Material.BIRCH_LOG |
				material == Material.DARK_OAK_LOG |
				material == Material.JUNGLE_LOG |
				material == Material.OAK_LOG |
				material == Material.SPRUCE_LOG) {
			return String.format("Chop %s %ss", goal, StringFormatter.format(material.toString()));
		}
		return String.format("Break %s %s", goal, StringFormatter.format(material.toString()));
	}
	
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.BREAK_BLOCK.name(), material.name(), materialString};
	}
}
