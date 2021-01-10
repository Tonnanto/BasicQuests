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
	ArrayList<String> questTypes;  // relevant only for Rewards
	HashMap<String, Double> variants;  // relevant only for Tool, Armor and Potion Rewards

}
