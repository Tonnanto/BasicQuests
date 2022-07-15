package de.stamme.basicquests.model.quests;

import org.bukkit.entity.EntityType;

import de.stamme.basicquests.util.StringFormatter;

public class EntityKillQuest extends Quest {


	// ---------------------------------------------------------------------------------------
	// Quest State
	// ---------------------------------------------------------------------------------------

	private final EntityType entity;


	// ---------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------

	public EntityKillQuest(EntityType ent, int goal, Reward reward) {
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
		return String.format("Kill %s %s%s", getGoal(), StringFormatter.format(entity.toString()), (getGoal() > 1) ? "s" : "");
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
}
