package de.stamme.basicquests.quests;

import org.bukkit.Material;

import de.stamme.basicquests.main.StringFormatter;

public class HarvestBlockQuest extends Quest {
	
	public Material material;

	public HarvestBlockQuest(Material mat, int goal, Reward reward) {
		super(goal, reward);
		this.material = mat;
	}
	
	public QuestData toData() {
		QuestData data = super.toData();
		
		data.questType = QuestType.HARVEST_BLOCK.name();
		data.material = material.name();
		
		return data;
	}

	// Returns a String in the format: "Harvest <amount> <material>"
	public String getName() {
		return String.format("Harvest %s %s", goal, StringFormatter.format(material.toString()));
	}
	
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.HARVEST_BLOCK.name(), material.name()};
	}
}
