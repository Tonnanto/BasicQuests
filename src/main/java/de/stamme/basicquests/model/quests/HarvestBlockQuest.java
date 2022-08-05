package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.util.L10n;
import org.bukkit.Material;

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

	/**
	 *
	 * @return String in the format: "Harvest <amount> <material>"
	 */
	@Override
	public String getName() {
		int goal = getGoal();
		if (goal <= 1) {
			String singularName = L10n.getMinecraftName(getOptionKey(), "block.minecraft.", "item.minecraft.");
			return MessageFormat.format(L10n.getMessage("quest.harvestBlock.singular"), singularName);
		} else {
			String pluralName = L10n.getLocalizedPluralName(getQuestType(), getOptionKey(), "block.minecraft.", "item.minecraft.");
			return MessageFormat.format(L10n.getMessage("quest.harvestBlock.plural"), goal, pluralName);
		}
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
	public String getOptionKey() {
		return material.toString();
	}
}
