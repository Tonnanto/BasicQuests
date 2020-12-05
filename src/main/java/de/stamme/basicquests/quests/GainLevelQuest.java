package de.stamme.basicquests.quests;

public class GainLevelQuest extends Quest {

	public GainLevelQuest(int goal, Reward reward) {
		super(goal, reward);
	}
	
	public QuestData toData() {
		QuestData data = super.toData();
		
		data.questType = QuestType.GAIN_LEVEL.name();

		return data;
	}
	
	// Returns a String in the format: "Level up <goal> times"
	public String getName() {
		return String.format("Level up %s times", goal);
	}
	
	public String[] getDecisionObjectNames() {
		String[] arr = {QuestType.GAIN_LEVEL.name()};
		return arr;
	}
}
