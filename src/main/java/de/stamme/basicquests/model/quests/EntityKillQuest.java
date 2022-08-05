package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.Main;
import java.text.MessageFormat;
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
		int goal = this.getGoal();
		String entityName = StringFormatter.getLocalizedName(this.entity.name(), "entity.minecraft.");
		return goal > 1
			? MessageFormat.format(Main.l10n("quest.killEntity.plural"), goal, entityName)
			: MessageFormat.format(Main.l10n("quest.killEntity.singular"), entityName);
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
