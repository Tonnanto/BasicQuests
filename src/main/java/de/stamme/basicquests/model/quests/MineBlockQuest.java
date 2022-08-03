package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.Main;
import org.bukkit.Material;

import de.stamme.basicquests.util.StringFormatter;

import java.text.MessageFormat;

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
		return MessageFormat.format(Main.l10n("quests.title.mineBlock"), this.getGoal(), Main.localizedMaterial(material));
	}

	@Override
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.MINE_BLOCK.name(), material.name()};
	}

	public Material getMaterial() {
		return material;
	}

	@Override
	public final QuestType getQuestType() {
		return QuestType.MINE_BLOCK;
	}

	@Override
	public String getOptionName() {
		return StringFormatter.format(material.toString());
	}
}
