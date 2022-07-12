package de.stamme.basicquests.questgeneration;

import java.util.List;
import java.util.HashMap;

public class GenerationOption {

	String name;
	double value, weight = 1;
	double value_base = 0;
	double value_per_unit = 1;
	double radius = 60;  // relevant only for FindStructureQuest
	int min = 1, max, step;
	List<GenerationOption> decisionObjects;
//	List<String> jobs;
	List<String> advancements;
	List<String> questTypes;  // relevant only for Rewards
	HashMap<String, Double> variants;  // relevant only for Tool, Armor and Potion Rewards

	@Override
	public String toString() {
		return "DecisionObject{" +
				"name='" + name + '\'' +
				", value=" + value +
				", weight=" + weight +
				", valueBase=" + value_base +
				", valuePerUnit=" + value_per_unit +
				", radius=" + radius +
				", min=" + min +
				", max=" + max +
				", step=" + step +
				", decisionObjects=" + decisionObjects +
				", advancements=" + advancements +
				", questTypes=" + questTypes +
				", variants=" + variants +
				'}';
	}
}
