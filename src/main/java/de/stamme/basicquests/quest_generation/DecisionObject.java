package de.stamme.basicquests.quest_generation;

import java.util.ArrayList;
import java.util.HashMap;

public class DecisionObject {

	String name;
	double value, weight = 1;
	double radius = 60;  // relevant only for FindStructureQuest
	int min = 1, max, step;
	ArrayList<DecisionObject> decisionObjects;
//	ArrayList<String> jobs;
	ArrayList<String> advancements;
	ArrayList<String> questTypes;  // relevant only for Tool or Armor Rewards
	HashMap<String, Double> materials;  // relevant only for Tool or Armor Rewards

}
