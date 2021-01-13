package de.stamme.basicquests.quests;

import org.bukkit.Material;

import de.stamme.basicquests.util.StringFormatter;

public class MineBlockQuest extends Quest {

	public Material material;

	public MineBlockQuest(Material mat, int goal, Reward reward) {
		super(goal, reward);
		this.material = mat;
	}
	
	public QuestData toData() {
		QuestData data = super.toData();
		
		data.questType = QuestType.MINE_BLOCK.name();
		data.material = material.name();
		
		return data;
	}

	// Returns a String in the format: "Mine <amount> <material>"
	public String getName() {
		return String.format("Mine %s %s", goal, StringFormatter.format(material.toString()));
	}
	
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.MINE_BLOCK.name(), material.name()};
	}

}
