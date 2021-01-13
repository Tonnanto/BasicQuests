package de.stamme.basicquests.quests;

import org.bukkit.entity.EntityType;

import de.stamme.basicquests.util.StringFormatter;

public class EntityKillQuest extends Quest {
	
	public EntityType entity;

	public EntityKillQuest(EntityType ent, int goal, Reward reward) {
		super(goal, reward);
		this.entity = ent;
	}
	
	public QuestData toData() {
		QuestData data = super.toData();
		
		data.questType = QuestType.KILL_ENTITY.name();
		data.entity = entity.name();

		return data;
	}
	
	// Returns a String in the format: "Kill <amount> <entity>"
	public String getName() {
		return String.format("Kill %s %s%s", goal, StringFormatter.format(entity.toString()), (goal > 1) ? "s" : "");
	}
	
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.KILL_ENTITY.name(), entity.name()};
	}
}
