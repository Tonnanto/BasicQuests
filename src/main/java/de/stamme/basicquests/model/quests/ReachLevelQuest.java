package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.QuestPlayer;
import java.text.MessageFormat;

public class ReachLevelQuest extends Quest {


	// ---------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------

	public ReachLevelQuest(QuestPlayer questPlayer, int goal, Reward reward) {
		super(goal, reward);
		this.setCount(questPlayer.getPlayer().getLevel());
	}

	public ReachLevelQuest(int goal, Reward reward) {
		super(goal, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Functionality
	// ---------------------------------------------------------------------------------------

	@Override
	public QuestData toData() {
		QuestData data = super.toData();
		data.setQuestType(QuestType.REACH_LEVEL.name());
		return data;
	}


	// ---------------------------------------------------------------------------------------
	// Getter & Setter
	// ---------------------------------------------------------------------------------------

	// Returns a String in the format: "Reach level <goal>"
	@Override
	public String getName() {
		return MessageFormat.format(Main.l10n("quest.reachLevel"), this.getGoal());
	}

	@Override
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.REACH_LEVEL.name()};
	}

	@Override
	public final QuestType getQuestType() {
		return QuestType.REACH_LEVEL;
	}

	@Override
	public String getOptionName() {
		return String.valueOf(this.getGoal());
	}
}
