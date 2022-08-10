package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.config.MinecraftLocaleConfig;
import de.stamme.basicquests.model.rewards.Reward;
import org.bukkit.entity.EntityType;

import java.text.MessageFormat;

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
		int goal = this.getGoal();
		if (goal <= 1) {
			String singularName = MinecraftLocaleConfig.getMinecraftName(getOptionKey(), "entity.minecraft.");
			return MessageFormat.format(MessagesConfig.getMessage("quests.kill-entity.singular"), singularName);
		} else {
			String pluralName = MessagesConfig.getPluralName(getQuestType(), getOptionKey(), "entity.minecraft.");
			return MessageFormat.format(MessagesConfig.getMessage("quests.kill-entity.plural"), goal, pluralName);
		}
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
	public String getOptionKey() {
		return entity.toString();
	}
}
