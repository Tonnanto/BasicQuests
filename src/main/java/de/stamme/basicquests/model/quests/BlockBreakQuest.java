package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.util.L10n;
import org.bukkit.Material;

import java.text.MessageFormat;

public class BlockBreakQuest extends Quest {


	// ---------------------------------------------------------------------------------------
	// Quest State
	// ---------------------------------------------------------------------------------------

	private final Material material;


	// ---------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------

	public BlockBreakQuest(Material mat, int goal, Reward reward) {
		super(goal, reward);
		this.material = mat;
	}


	// ---------------------------------------------------------------------------------------
	// Functionality
	// ---------------------------------------------------------------------------------------

	@Override
	public QuestData toData() {
		QuestData data = super.toData();
		data.setQuestType(QuestType.BREAK_BLOCK.name());
		data.setMaterial(material.name());
		return data;
	}


	// ---------------------------------------------------------------------------------------
	// Getter & Setter
	// ---------------------------------------------------------------------------------------

	/**
	 * @return String in the format: "Break <amount> <material>"
 	 */
	@Override
	public String getName() {
		int goal = getGoal();
		if (goal <= 1) {
			String singularName = L10n.getMinecraftName(getOptionKey(), "block.minecraft.");
			return MessageFormat.format(L10n.getMessage("quest.breakBlock.singular"), singularName);
		} else {
			String pluralName = L10n.getLocalizedPluralName(getQuestType(), getOptionKey(), "block.minecraft.");
			return MessageFormat.format(L10n.getMessage("quest.breakBlock.plural"), goal, pluralName);
		}
	}

	@Override
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.BREAK_BLOCK.name(), material.name()};
	}

	@Override
	public String getOptionKey() {
		return material.toString();
	}

	public Material getMaterial() {
		return material;
	}

	@Override
	public final QuestType getQuestType() {
		return QuestType.BREAK_BLOCK;
	}
}
