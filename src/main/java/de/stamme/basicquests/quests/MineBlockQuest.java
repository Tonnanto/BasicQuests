package de.stamme.basicquests.quests;

import org.bukkit.Material;

import de.stamme.basicquests.util.StringFormatter;

public class MineBlockQuest extends Quest {


	// ---------------------------------------------------------------------------------------
	// Quest State
	// ---------------------------------------------------------------------------------------

	private final Material material;


	// ---------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------

	public MineBlockQuest(Material mat, int goal, Reward reward) {
		super(goal, reward);
		this.material = mat;
	}


	// ---------------------------------------------------------------------------------------
	// Functionality
	// ---------------------------------------------------------------------------------------

	@Override
	public QuestData toData() {
		QuestData data = super.toData();
		data.setQuestType(QuestType.MINE_BLOCK.name());
		data.setMaterial(material.name());
		return data;
	}


	// ---------------------------------------------------------------------------------------
	// Getter & Setter
	// ---------------------------------------------------------------------------------------

	/**
	 * @return String in the format: "Mine <amount> <material>"
	 */
	@Override
	public String getName() {
		return String.format("Mine %s %s", getGoal(), StringFormatter.format(material.toString()));
	}

	@Override
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.MINE_BLOCK.name(), material.name()};
	}

	public Material getMaterial() {
		return material;
	}
}
