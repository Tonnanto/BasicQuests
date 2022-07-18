package de.stamme.basicquests.model.quests;

import org.bukkit.Material;

import de.stamme.basicquests.util.StringFormatter;

public class HarvestBlockQuest extends Quest {


	// ---------------------------------------------------------------------------------------
	// Quest State
	// ---------------------------------------------------------------------------------------

	private final Material material;


	// ---------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------

	public HarvestBlockQuest(Material mat, int goal, Reward reward) {
		super(goal, reward);
		this.material = mat;
	}


	// ---------------------------------------------------------------------------------------
	// Functionality
	// ---------------------------------------------------------------------------------------

	@Override
	public QuestData toData() {
		QuestData data = super.toData();
		data.setQuestType(QuestType.HARVEST_BLOCK.name());
		data.setMaterial(material.name());
		return data;
	}


	// ---------------------------------------------------------------------------------------
	// Getter & Setter
	// ---------------------------------------------------------------------------------------

	// Returns a

	/**
	 *
	 * @return String in the format: "Harvest <amount> <material>"
	 */
	@Override
	public String getName() {
		return String.format("Harvest %s %s", getGoal(), StringFormatter.format(material.toString()));
	}

	@Override
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.HARVEST_BLOCK.name(), material.name()};
	}

	public Material getMaterial() {
		return material;
	}

	@Override
	public final QuestType getQuestType() {
		return QuestType.HARVEST_BLOCK;
	}

	@Override
	public String getOptionName() {
		return StringFormatter.format(material.toString());
	}
}
