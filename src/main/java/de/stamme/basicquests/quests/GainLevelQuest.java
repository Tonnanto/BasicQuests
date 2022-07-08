package de.stamme.basicquests.quests;

public class GainLevelQuest extends Quest {


	// ---------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------

	public GainLevelQuest(int goal, Reward reward) {
		super(goal, reward);
	}


	// ---------------------------------------------------------------------------------------
	// Functionality
	// ---------------------------------------------------------------------------------------

	@Override
	public QuestData toData() {
		QuestData data = super.toData();
		data.setQuestType(QuestType.GAIN_LEVEL.name());
		return data;
	}


	// ---------------------------------------------------------------------------------------
	// Getter & Setter
	// ---------------------------------------------------------------------------------------

	/**
	 * @return String in the format: "Level up <goal> times"
	 */
	@Override
	public String getName() {
		return String.format("Level up %s times", getGoal());
	}

	@Override
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.GAIN_LEVEL.name()};
	}
}
