package de.stamme.basicquests.quests;

import de.stamme.basicquests.main.QuestPlayer;

public class ReachLevelQuest extends Quest {

	public ReachLevelQuest(QuestPlayer player, int goal, Reward reward) {
		super(goal, reward);
		this.count = player.player.getLevel();
	}
	
	public QuestData toData() {
		QuestData data = super.toData();
		
		data.questType = QuestType.REACH_LEVEL.name();

		return data;
	}
	
	
	public ReachLevelQuest(int goal, Reward reward) {
		super(goal, reward);
	}

	// Returns a String in the format: "Reach level <goal>"
	public String getName() {
		return String.format("Reach level %s", goal);
	}
	
	public String[] getDecisionObjectNames() {
		String[] arr = {QuestType.REACH_LEVEL.name()};
		return arr;
	}
	
}
