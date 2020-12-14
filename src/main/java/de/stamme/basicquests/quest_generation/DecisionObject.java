package de.stamme.basicquests.quest_generation;

import java.util.ArrayList;

public class DecisionObject {

	String name;
	double value, weight = 1;
	double radius = 60;  // relevant only for FindStructureQuest
	int min, max, step;
	ArrayList<DecisionObject> decisionObjects;
//	ArrayList<String> jobs;
	ArrayList<String> advancements;
	
}
