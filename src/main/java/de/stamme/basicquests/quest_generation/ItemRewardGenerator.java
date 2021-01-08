package de.stamme.basicquests.quest_generation;

import de.stamme.basicquests.main.JsonManager;
import de.stamme.basicquests.quests.QuestType;
import de.stamme.basicquests.quests.Reward;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class ItemRewardGenerator {

    private static final String tool_rewards_path = "/quest_generation/item_reward_generation/tool_rewards.json";
    private static final String armor_rewards_path = "/quest_generation/item_reward_generation/armor_rewards.json";
    private static final String enchantment_rewards_path = "/quest_generation/item_reward_generation/enchantment_rewards.json";
    private static final String resource_rewards_path = "/quest_generation/item_reward_generation/resource_rewards.json";
    private static final String other_item_rewards_path = "/quest_generation/item_reward_generation/other_item_rewards.json";

    private static final double questTypeWeightFactor = 2;

    public static void main(String[] args) {
        Random r = new Random();

        ArrayList<DecisionObject> toolRewardsMap = JsonManager.getDecisionObjects(tool_rewards_path);
        ArrayList<DecisionObject> armorRewardsMap = JsonManager.getDecisionObjects(armor_rewards_path);
        ArrayList<DecisionObject> enchantmentRewardsMap = JsonManager.getDecisionObjects(enchantment_rewards_path);
        ArrayList<DecisionObject> resourceRewardsMap = JsonManager.getDecisionObjects(resource_rewards_path);
        ArrayList<DecisionObject> otherItemRewardsMap = JsonManager.getDecisionObjects(other_item_rewards_path);

        ArrayList<DecisionObject> decisionObjects = new ArrayList<DecisionObject>();
        decisionObjects.addAll(toolRewardsMap);
        decisionObjects.addAll(armorRewardsMap);
        decisionObjects.addAll(enchantmentRewardsMap);
        decisionObjects.addAll(resourceRewardsMap);
        decisionObjects.addAll(otherItemRewardsMap);


        for (int i = 0; i < 100; i++) {
            try {
                DecisionObject obj = decide(decisionObjects, QuestType.ENCHANT_ITEM, 1000);
                System.out.println(obj.name);
            } catch(Exception e) {
                System.out.print(e.getMessage());
            }
        }
    }

    public static DecisionObject decide(ArrayList<DecisionObject> objects, QuestType questType, double questValue) {

//      TODO: Consider QuestType and quest value to adjust weights of DecisionObjects

        return QuestGenerator.decide(objects);
    }

    public static Reward generate(QuestType questType, double questValue) throws Exception {

//      Choose a random reward
//      if reward.value > quest.value * 1.5 -> decrease rewards value or choose new reward
//      if reward.value < quest.value * 0.8 -> increase it's value or add another item to the reward

        double minValue = questValue * 0.8;
        double maxValue = questValue * 1.5;

        ArrayList<DecisionObject> toolRewardsMap = JsonManager.getDecisionObjects(tool_rewards_path);
        ArrayList<DecisionObject> armorRewardsMap = JsonManager.getDecisionObjects(armor_rewards_path);
        ArrayList<DecisionObject> enchantmentRewardsMap = JsonManager.getDecisionObjects(enchantment_rewards_path);
        ArrayList<DecisionObject> resourceRewardsMap = JsonManager.getDecisionObjects(resource_rewards_path);
        ArrayList<DecisionObject> otherItemRewardsMap = JsonManager.getDecisionObjects(other_item_rewards_path);

        ArrayList<DecisionObject> decisionObjects = new ArrayList<DecisionObject>();
        decisionObjects.addAll(toolRewardsMap);
        decisionObjects.addAll(armorRewardsMap);
        decisionObjects.addAll(enchantmentRewardsMap);
        decisionObjects.addAll(resourceRewardsMap);
        decisionObjects.addAll(otherItemRewardsMap);


        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        double rewardValue = 0;

        do {
            ItemStack item = null;
            double itemValue = 0;
            double minItemValue = minValue - rewardValue;
            double maxItemValue = maxValue - rewardValue;

            DecisionObject materialDO = decide(decisionObjects, questType, questValue);
            String material = "";
            double materialValue = 0;
            int amount = materialDO.min;

            DecisionObject enchantmentDO = null;
            Enchantment enchantment = null;
            double enchantmentValue = 0;
            int enchantmentLevel = 1;
            int maxEnchantmentLevel = 1;

//          TODO: Handle the case that all possible rewards have itemValue > maxValue


//            Choose lowest Material when available for the selected item
            if (materialDO.materials != null) {
                Optional<Map.Entry<String, Double>> lowestMat = materialDO.materials.entrySet().stream().min(Map.Entry.comparingByValue());
                material = lowestMat.isPresent() ? lowestMat.get().getKey() : "";
                materialValue = lowestMat.isPresent() ? lowestMat.get().getValue() : materialDO.value;
            } else {
                materialValue = materialDO.value;
            }

            itemValue = getValue(materialValue, amount, enchantmentValue, enchantmentLevel);

//            Regenerate if itemValue > maxValue
            if (itemValue > maxItemValue) { break; }


//            Choose Enchantment when available for the selected item
            if (materialDO.decisionObjects != null) {
                enchantmentDO = decide(materialDO.decisionObjects, questType, questValue);
                assert enchantmentDO != null;

                enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantmentDO.name.toLowerCase()));

                if (enchantment != null) {
                    maxEnchantmentLevel = enchantment.getMaxLevel();
                    if (maxEnchantmentLevel > 1) {
                        Random r = new Random();
                        enchantmentLevel = r.nextInt(enchantment.getMaxLevel()) + 1;
                    }

                    enchantmentValue = enchantmentDO.value;
                }
            }

            itemValue = getValue(materialValue, amount, enchantmentValue, enchantmentLevel);


//            Increase material and enchantment level (if available) as long as itemValue < minItemValue
            if(!material.isEmpty() || enchantment != null) {
                while (itemValue < minItemValue && (!material.equalsIgnoreCase("NETHERITE") || enchantmentLevel < maxEnchantmentLevel)) {

                    String newMaterial = material;
                    int newEnchantmentLevel = enchantmentLevel;

                    double newMaterialValue = materialValue;
                    double newItemValue = materialValue * amount + enchantmentValue;


                    if(!material.isEmpty() && !material.equalsIgnoreCase("NETHERITE")) {
                        switch (material) {
                            case "CHAIN":
                                newMaterial = "IRON";
                                break;
                            case "IRON":
                                newMaterial = "DIAMOND";
                                break;
                            case "DIAMOND":
                                newMaterial = "NETHERITE";
                                break;
                            default:
                                break;
                        }

                        newMaterialValue = materialDO.materials.get(newMaterial);
                        newItemValue = getValue(newMaterialValue, amount, enchantmentValue, newEnchantmentLevel);

                    }

                    if (newItemValue < minItemValue) {
                        if (enchantment != null && enchantmentLevel < maxEnchantmentLevel) {
                            newEnchantmentLevel++;
                            newItemValue = getValue(newMaterialValue, amount, enchantmentValue, newEnchantmentLevel);
                        }
                    }

                    material = newMaterial;
                    materialValue = newMaterialValue;
                    enchantmentLevel = newEnchantmentLevel;

                }
            }


//          TODO: increase amount if itemValue < minItemValue



            if (item != null) {
                rewardValue += itemValue;
                items.add(item);
            }

        } while(rewardValue < minValue);


        return new Reward(items);
    }

    private static double getValue(double materialValue, int amount, double enchantmentValue, int enchantmentLevel) {
        return materialValue * amount + enchantmentValue * enchantmentLevel;
    }
}
