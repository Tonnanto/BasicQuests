package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.wrapper.material.QuestMaterialService;
import org.bukkit.Material;

import de.stamme.basicquests.util.StringFormatter;

import java.text.MessageFormat;

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
		return MessageFormat.format(Main.l10n("quests.title.harvestBlock"), this.getGoal(), StringFormatter.localizedMaterial(material));
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
