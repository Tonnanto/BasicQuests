package de.stamme.basicquests.model.generation;

import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;

public class GenerationOption {

	private String name;
	private double value, weight = 1;
	private double value_base = 0;
	private double value_per_unit = 1;
	private double radius = 60;  // relevant only for FindStructureQuest
	private int min = 1, max, step;
	private List<GenerationOption> options;
//	private List<String> jobs;
	private List<String> advancements;
	private List<String> questTypes;  // relevant only for Rewards
	private HashMap<String, Double> variants;  // relevant only for Tool, Armor and Potion Rewards

	public double getValue(int units) {
		return value_base + value_per_unit * units;
	}

	@Override
	public GenerationOption clone() {
		GenerationOption clone = new GenerationOption();
		clone.name = name;
		clone.value = value;
		clone.weight = weight;
		clone.value_base = value_base;
		clone.value_per_unit = value_per_unit;
		clone.radius = radius;
		clone.min = min;
		clone.max = max;
		clone.step = step;
		clone.advancements = advancements;
		clone.questTypes = questTypes;
		clone.variants = variants;

		if (options != null && !options.isEmpty()) {
			clone.options = options.stream().map(GenerationOption::clone).collect(Collectors.toList());
		}

		return clone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getValue_base() {
		return value_base;
	}

	public void setValue_base(double value_base) {
		this.value_base = value_base;
	}

	public double getValue_per_unit() {
		return value_per_unit;
	}

	public void setValue_per_unit(double value_per_unit) {
		this.value_per_unit = value_per_unit;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public List<GenerationOption> getOptions() {
		return options;
	}

	public void setOptions(List<GenerationOption> options) {
		this.options = options;
	}

	public List<String> getAdvancements() {
		return advancements;
	}

	public void setAdvancements(List<String> advancements) {
		this.advancements = advancements;
	}

	public List<String> getQuestTypes() {
		return questTypes;
	}

	public void setQuestTypes(List<String> questTypes) {
		this.questTypes = questTypes;
	}

	public HashMap<String, Double> getVariants() {
		return variants;
	}

	public void setVariants(HashMap<String, Double> variants) {
		this.variants = variants;
	}

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
				", options=" + options +
				", advancements=" + advancements +
				", questTypes=" + questTypes +
				", variants=" + variants +
				'}';
	}
}
