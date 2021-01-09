package de.stamme.basicquests.quest_generation;

import de.stamme.basicquests.main.JsonManager;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.quests.QuestType;
import de.stamme.basicquests.quests.Reward;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class ItemRewardGenerator {

    private static final String tool_rewards_path = "/quest_generation/item_reward_generation/tool_rewards.json";
    private static final String armor_rewards_path = "/quest_generation/item_reward_generation/armor_rewards.json";
    private static final String enchantment_rewards_path = "/quest_generation/item_reward_generation/enchantment_rewards.json";
    private static final String resource_rewards_path = "/quest_generation/item_reward_generation/resource_rewards.json";
    private static final String other_item_rewards_path = "/quest_generation/item_reward_generation/other_item_rewards.json";

    private static final double questTypeWeightFactor = 2;
    private static final double lowestMaxValue = 160;

//    Test Purpose
    public static void main(String[] args) {


        for (int i = 0; i < 100; i++) {
            try {
                Reward reward = generate(QuestType.ENCHANT_ITEM, 10);
                for (ItemStack item: reward.items) {
                    System.out.println(item.getAmount() + " " + item.getType().name());
                }
            } catch(Exception e) {
                System.out.print(e.getMessage());
            }
        }
    }

//    Test Purpose
    public static void test() {

        for (int i = 0; i < 50; i++) {
            try {
                Reward reward = generate(QuestType.ENCHANT_ITEM, 1000);
                System.out.println(reward.toString());

            } catch(Exception e) {
                System.out.print(e.getMessage());
            }
        }
    }

    public static DecisionObject decide(ArrayList<DecisionObject> objects, QuestType questType, double maxValue) {

        for (DecisionObject obj: objects) {
//            Remove DecisionObjects where minValue > maxRewardValue
            double minValue;
            if (obj.materials != null) {
                Optional<Map.Entry<String, Double>> lowestMat = obj.materials.entrySet().stream().min(Map.Entry.comparingByValue());
                minValue = obj.min * (lowestMat.isPresent() ? lowestMat.get().getValue() : obj.value);
            } else {
                minValue = obj.min * obj.value;
            }

            if (minValue > maxValue && minValue > lowestMaxValue) {
                obj.weight = 0;

//            Adjust DecisionObjects weight if the QuestType matches
            } else if (obj.questTypes != null) {
                for (String questTypeString: obj.questTypes) {
                    if (questTypeString.equalsIgnoreCase(questType.name())) {
                        obj.weight *= questTypeWeightFactor;
                        break;
                    }
                }
            }
        }

        return QuestGenerator.decide(objects);
    }

    public static Reward generate(QuestType questType, double questValue) {

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

        ArrayList<RewardItem> items = new ArrayList<>();
        double rewardValue = 0;

        do {
            ItemStack item = null;
            double itemValue = 0;
            double minItemValue = minValue - rewardValue;
            double maxItemValue = maxValue - rewardValue;

            DecisionObject materialDO = decide(decisionObjects, questType, questValue);
            String materialString = "";
            double materialValue = 0;
            int amount = (materialDO.min > 0) ? materialDO.min : 1;

            DecisionObject enchantmentDO = null;
            Enchantment enchantment = null;
            double enchantmentValue = 0;
            int enchantmentLevel = 1;
            int maxEnchantmentLevel = 1;

//            Choose lowest Material when available for the selected item
            if (materialDO.materials != null) {
                Optional<Map.Entry<String, Double>> lowestMat = materialDO.materials.entrySet().stream().min(Map.Entry.comparingByValue());
                materialString = lowestMat.isPresent() ? lowestMat.get().getKey() : "";
                materialValue = lowestMat.isPresent() ? lowestMat.get().getValue() : materialDO.value;
            } else {
                materialValue = materialDO.value;
            }

            itemValue = getValue(materialValue, amount, enchantmentValue, enchantmentLevel);

//            Regenerate if itemValue > maxValue
            if (itemValue > lowestMaxValue && itemValue > maxItemValue) {
                continue;
            }


            if (enchantmentRewardsMap.contains(materialDO)) {
//                Enchanted Book

                enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(materialDO.name.toLowerCase()));
                if (enchantment != null) {
                    maxEnchantmentLevel = enchantment.getMaxLevel();
                    if (maxEnchantmentLevel > 1) {
                        Random r = new Random();
                        enchantmentLevel = r.nextInt(enchantment.getMaxLevel()) + 1;
                    }

                    enchantmentValue = materialDO.value;
                } else {
                    Main.log("Could not find Enchantment with name: " + materialDO.name);
                    continue;
                }

//                This itemValue calculation works only for enchanted Books
                itemValue = getValue(0, 0, materialDO.value, enchantmentLevel * amount);


                while (itemValue < minItemValue && enchantmentLevel < maxEnchantmentLevel) {
                    double newItemValue = getValue(0, 0, materialDO.value, (enchantmentLevel + 1) * amount);
                    if (newItemValue < maxItemValue) {
                        enchantmentLevel++;
                        itemValue = newItemValue;
                    }
                }

//                Increase amount if necessary and allowed
                while (itemValue < minItemValue && amount < materialDO.max) {
                    amount += materialDO.step;
                    itemValue = getValue(0, 0, materialDO.value, enchantmentLevel * amount);
                }




            } else {
//                 No Enchanted Book
                // Choose Enchantment when available for the selected item
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

                // Increase material and enchantment level (if available) as long as itemValue < minItemValue
                if (!materialString.isEmpty() || enchantment != null) {
                    while (itemValue < minItemValue && (!materialString.equalsIgnoreCase("NETHERITE") || enchantmentLevel < maxEnchantmentLevel)) {

                        String newMaterialString = materialString;
                        int newEnchantmentLevel = enchantmentLevel;

                        double newMaterialValue;
                        double oldItemValue = itemValue;
                        double newItemValue;


                        if (!materialString.isEmpty() && !materialString.equalsIgnoreCase("NETHERITE")) {
                            switch (materialString) {
                                case "CHAINMAIL":
                                    newMaterialString = "IRON";
                                    break;
                                case "IRON":
                                    newMaterialString = "DIAMOND";
                                    break;
                                case "DIAMOND":
                                    newMaterialString = "NETHERITE";
                                    break;
                                default:
                                    break;
                            }

                            newMaterialValue = materialDO.materials.get(newMaterialString);
                            newItemValue = getValue(newMaterialValue, amount, enchantmentValue, enchantmentLevel);

                            if (newItemValue < maxItemValue) {
                                materialString = newMaterialString;
                                materialValue = newMaterialValue;
                                itemValue = newItemValue;
                            }

                        }

                        if (itemValue < minItemValue) {
                            if (enchantment != null && enchantmentLevel < maxEnchantmentLevel) {
                                newEnchantmentLevel++;
                                newItemValue = getValue(materialValue, amount, enchantmentValue, newEnchantmentLevel);

                                if (newItemValue < maxItemValue) {
                                    enchantmentLevel = newEnchantmentLevel;
                                    itemValue = newItemValue;
                                }
                            }
                        }

                        // Kill loop if nothing has changed
                        if (oldItemValue == itemValue)
                            break;

                    }
                }

//                Increase amount if necessary and allowed
                while (itemValue < minItemValue && amount < materialDO.max) {
                    amount += materialDO.step;
                    itemValue = getValue(materialValue, amount, enchantmentValue, enchantmentLevel);
                }
            }




//            Initialize ItemStack accordingly
            Material material;

            if (enchantmentRewardsMap.contains(materialDO)) {
                material = Material.ENCHANTED_BOOK;
                item = new ItemStack(material, amount);

                EnchantmentStorageMeta meta = (EnchantmentStorageMeta)item.getItemMeta();
                if (meta != null) {
                    assert enchantment != null;
                    meta.addStoredEnchant(enchantment, enchantmentLevel, true);
                    item.setItemMeta(meta);
                } else
                    continue;

            } else {
                if (!materialString.isEmpty())
                    materialString += "_" + materialDO.name;
                else
                    materialString = materialDO.name;

                material = Material.getMaterial(materialString);

                if (material == null) {
                    Main.log("Could not find Material with name: " + materialString);
                    continue;
                }

                item = new ItemStack(material, amount);

                if (enchantment != null)
                    item.addEnchantment(enchantment, enchantmentLevel);
            }



            rewardValue += itemValue;
            items.add(new RewardItem(item, itemValue));

//            Prevent this Material from reappearing in this reward
            materialDO.weight = 0;

        } while(rewardValue < minValue);

        System.out.println("Reward Value: " + rewardValue);
        return new Reward(new ArrayList<>(items.stream().sorted().map(x->x.item).collect(Collectors.toList())));
    }

    private static double getValue(double materialValue, int amount, double enchantmentValue, int enchantmentLevel) {
        return materialValue * amount + enchantmentValue * enchantmentLevel;
    }
}
