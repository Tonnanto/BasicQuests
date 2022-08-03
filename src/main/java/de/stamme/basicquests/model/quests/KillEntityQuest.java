package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.Main;
import org.bukkit.entity.EntityType;

import de.stamme.basicquests.util.StringFormatter;

import java.text.MessageFormat;

public class KillEntityQuest extends Quest {


	// ---------------------------------------------------------------------------------------
	// Quest State
	// ---------------------------------------------------------------------------------------

	private final EntityType entity;


	// ---------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------

	public KillEntityQuest(EntityType ent, int goal, Reward reward) {
		super(goal, reward);
		this.entity = ent;
	}


	// ---------------------------------------------------------------------------------------
	// Functionality
	// ---------------------------------------------------------------------------------------

	@Override
	public QuestData toData() {
		QuestData data = super.toData();
		data.setQuestType(QuestType.KILL_ENTITY.name());
		data.setEntity(entity.name());
		return data;
	}


	// ---------------------------------------------------------------------------------------
	// Getter & Setter
	// ---------------------------------------------------------------------------------------

	/**
	 * @return String in the format: "Kill <amount> <entity>"
	 */
	@Override
	public String getName() {
		return MessageFormat.format(Main.l10n("quests.title.killEntity"), this.getGoal(), Main.localizedEntity(entity));
	}
	
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.KILL_ENTITY.name(), entity.name()};
	}

	public EntityType getEntity() {
		return entity;
	}

	@Override
	public final QuestType getQuestType() {
		return QuestType.KILL_ENTITY;
	}

	@Override
	public String getOptionName() {
		return StringFormatter.format(entity.toString());
	}
}
