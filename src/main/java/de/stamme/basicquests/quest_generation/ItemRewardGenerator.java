package de.stamme.basicquests.quest_generation;

import de.stamme.basicquests.data.Config;
import de.stamme.basicquests.data.JsonManager;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.quests.QuestType;
import de.stamme.basicquests.quests.Reward;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

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
    private static final String food_rewards_path = "/quest_generation/item_reward_generation/food_rewards.json";
    private static final String potion_rewards_path = "/quest_generation/item_reward_generation/potion_rewards.json";
    private static final String other_item_rewards_path = "/quest_generation/item_reward_generation/other_item_rewards.json";

    private static final double questTypeWeightFactor = 2;
    private static final double lowestMaxValue = 200;
    private static final double splashPotionValue = 32;
    private static final double splashPotionChance = 0.5;
    private static final double rewardAlreadyInQuestsFactor = 0.01;

    //    Test Purpose
    public static void main(String[] args) {

//        for (int i = 0; i < 100; i++) {
//            try {
//                Reward reward = generate(QuestType.ENCHANT_ITEM, 1000);
//                for (ItemStack item : reward.items) {
//                    System.out.println(item.getAmount() + " " + item.getType().name());
//                }
//            } catch (Exception e) {
//                System.out.print(e.getMessage());
//            }
//        }
    }


    public static DecisionObject decide(ArrayList<DecisionObject> objects, QuestType questType, double maxValue, ArrayList<String> rewardsInPlayerQuests) {

        for (DecisionObject obj : objects) {
//            Remove DecisionObjects where minValue > maxRewardValue
            double minValue;
            if (obj.variants != null) {
                Optional<Map.Entry<String, Double>> lowestMat = obj.variants.entrySet().stream().min(Map.Entry.comparingByValue());
                minValue = obj.min * (lowestMat.isPresent() ? lowestMat.get().getValue() : obj.value);
            } else {
                minValue = obj.min * obj.value;
            }

            if (minValue > maxValue && minValue > lowestMaxValue) {
                obj.weight = 0;

//            Adjust DecisionObjects weight if the Material is already in a Reward for the Player
            } else if (rewardsInPlayerQuests != null && rewardsInPlayerQuests.contains(obj.name)) {
                obj.weight *= rewardAlreadyInQuestsFactor;

//            Adjust DecisionObjects weight if the QuestType matches
            } else if (obj.questTypes != null && questType != null) {
                for (String questTypeString : obj.questTypes) {
                    if (questTypeString.equalsIgnoreCase(questType.name())) {
                        obj.weight *= questTypeWeightFactor;
                        break;
                    }
                }
            }
        }

        return QuestGenerator.decide(objects);
    }

    public static Reward generate(QuestType questType, double questValue, ArrayList<String> rewardsInPlayerQuests) {

//      Choose a random reward
//      if reward.value > quest.value * 1.5 -> decrease rewards value or choose new reward
//      if reward.value < quest.value * 0.8 -> increase it's value or add another item to the reward

        double minValue = questValue * 0.8;
        double maxValue = questValue * 1.5;

        ArrayList<DecisionObject> toolRewardsList = JsonManager.getDecisionObjects(tool_rewards_path);
        ArrayList<DecisionObject> armorRewardsList = JsonManager.getDecisionObjects(armor_rewards_path);
        ArrayList<DecisionObject> enchantmentRewardsList = JsonManager.getDecisionObjects(enchantment_rewards_path);
        ArrayList<DecisionObject> resourceRewardsList = JsonManager.getDecisionObjects(resource_rewards_path);
        ArrayList<DecisionObject> foodRewardsList = JsonManager.getDecisionObjects(food_rewards_path);
        ArrayList<DecisionObject> potionRewardsList = JsonManager.getDecisionObjects(potion_rewards_path);
        ArrayList<DecisionObject> otherItemRewardsList = JsonManager.getDecisionObjects(other_item_rewards_path);

        ArrayList<DecisionObject> decisionObjects = new ArrayList<>();
        decisionObjects.addAll(toolRewardsList);
        decisionObjects.addAll(armorRewardsList);
        decisionObjects.addAll(enchantmentRewardsList);
        decisionObjects.addAll(resourceRewardsList);
        decisionObjects.addAll(foodRewardsList);
        decisionObjects.addAll(potionRewardsList);
        decisionObjects.addAll(otherItemRewardsList);

        ArrayList<RewardItem> items = new ArrayList<>();
        ArrayList<String> materialNames = new ArrayList<>();
        double rewardValue = 0;

        do {
            double minItemValue = minValue - rewardValue;
            double maxItemValue = maxValue - rewardValue;

            DecisionObject materialDO = decide(decisionObjects, questType, maxValue, rewardsInPlayerQuests);
            if (materialDO == null) break;

            RewardItem rewardItem;

            if (toolRewardsList.contains(materialDO) || armorRewardsList.contains(materialDO)) {
                rewardItem = getToolArmorReward(materialDO, minItemValue, maxItemValue);

            } else if (enchantmentRewardsList.contains(materialDO)) {
                rewardItem = getEnchantmentReward(materialDO, minItemValue, maxItemValue);

            } else if (potionRewardsList.contains(materialDO)) {
                rewardItem = getPotionReward(materialDO, maxItemValue);

            } else {
                rewardItem = getReward(materialDO, maxItemValue);
            }

            // Prevent this Material from reappearing in this reward
            materialDO.weight = 0;

            if (rewardItem == null) {
                continue;
            }

            rewardValue += rewardItem.value;
            items.add(rewardItem);
            materialNames.add(materialDO.name);


        } while (rewardValue < minValue);

        Reward reward = new Reward(new ArrayList<>(items.stream().sorted().map(x -> x.item).collect(Collectors.toList())));
        reward.materialNames = materialNames;
        return reward;
    }

    private static RewardItem getReward(DecisionObject materialDO, double maxValue) {

        ItemStack item;
        double itemValue;

        Material material;
        int amount = (materialDO.min > 0) ? materialDO.min : 1;

        double maxAmountFactor = Config.getQuantityFactor() * Config.getRewardFactor() * 0.75;
        while (getValue(materialDO.value, amount + materialDO.step) < maxValue && amount < materialDO.max * maxAmountFactor) {
            amount += materialDO.step;
        }
        itemValue = getValue(materialDO.value, amount);

        material = Material.getMaterial(materialDO.name);

        if (material == null) {
            Main.log("Could not find Material with name: " + materialDO.name);
            return null;
        }

        item = new ItemStack(material, amount);

        return new RewardItem(item, itemValue);
    }

    private static RewardItem getToolArmorReward(DecisionObject materialDO, double minValue, double maxValue) {

        ItemStack item;
        double itemValue;

        String materialString = "";
        double materialValue;
        int amount = (materialDO.min > 0) ? materialDO.min : 1;

        DecisionObject enchantmentDO;
        Enchantment enchantment = null;
        double enchantmentValue = 0;
        int enchantmentLevel = 1;
        int maxEnchantmentLevel = 1;

//        Choose lowest Material when available for the selected item
        if (materialDO.variants != null) {
            Optional<Map.Entry<String, Double>> lowestMat = materialDO.variants.entrySet().stream().min(Map.Entry.comparingByValue());
            materialString = lowestMat.isPresent() ? lowestMat.get().getKey() : "";
            materialValue = lowestMat.isPresent() ? lowestMat.get().getValue() : materialDO.value;
        } else {
            materialValue = materialDO.value;
        }

//        Choose Enchantment when available for the selected item
        if (materialDO.decisionObjects != null) {
            enchantmentDO = decide(materialDO.decisionObjects, null, maxValue - materialValue, null);
            assert enchantmentDO != null;

            enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantmentDO.name.toLowerCase()));

            if (enchantment != null) {
                maxEnchantmentLevel = enchantment.getMaxLevel();
                if (maxEnchantmentLevel > 1) {
                    Random r = new Random();
                    enchantmentLevel = r.nextInt(enchantment.getMaxLevel() - 1) + 1;
                }

                enchantmentValue = enchantmentDO.value;
            }
        }

        itemValue = getValue(materialValue, amount, enchantmentValue, enchantmentLevel);

        // Increase material and enchantment level (if available) as long as itemValue < minItemValue
        if (!materialString.isEmpty() || enchantment != null) {
            while (itemValue < minValue && (!materialString.equalsIgnoreCase("NETHERITE") || enchantmentLevel < maxEnchantmentLevel)) {

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

                    newMaterialValue = materialDO.variants.get(newMaterialString);
                    newItemValue = getValue(newMaterialValue, amount, enchantmentValue, enchantmentLevel);

                    if (newItemValue < maxValue) {
                        materialString = newMaterialString;
                        materialValue = newMaterialValue;
                        itemValue = newItemValue;
                    }

                }

                if (itemValue < minValue) {
                    if (enchantment != null && enchantmentLevel < maxEnchantmentLevel) {
                        newEnchantmentLevel++;
                        newItemValue = getValue(materialValue, amount, enchantmentValue, newEnchantmentLevel);

                        if (newItemValue < maxValue) {
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

//            Increase amount if necessary and allowed
        double maxAmountFactor = Config.getQuantityFactor() * Config.getRewardFactor() * 0.75;
        while (getValue(materialValue, amount + materialDO.step, enchantmentValue, enchantmentLevel) < maxValue && amount < materialDO.max * maxAmountFactor) {
            amount += materialDO.step;
        }
        itemValue = getValue(materialValue, amount, enchantmentValue, enchantmentLevel);

        Material material;

        if (!materialString.isEmpty())
            materialString += "_" + materialDO.name;
        else
            materialString = materialDO.name;

        material = Material.getMaterial(materialString);

        if (material == null) {
            Main.log("Could not find Material with name: " + materialString);
            return null;
        }

        item = new ItemStack(material, amount);

        if (enchantment != null)
            item.addEnchantment(enchantment, enchantmentLevel);

        return new RewardItem(item, itemValue);
    }

    private static RewardItem getEnchantmentReward(DecisionObject enchantmentDO, double minValue, double maxValue) {

        ItemStack item;
        double itemValue;

        int amount = (enchantmentDO.min > 0) ? enchantmentDO.min : 1;

        Enchantment enchantment;
        double enchantmentValue;
        int enchantmentLevel = 1;
        int maxEnchantmentLevel;

        enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantmentDO.name.toLowerCase()));

        if (enchantment != null) {
            maxEnchantmentLevel = enchantment.getMaxLevel();
            if (maxEnchantmentLevel > 1) {
                Random r = new Random();
                enchantmentLevel = r.nextInt(enchantment.getMaxLevel()) + 1;
            }

            enchantmentValue = enchantmentDO.value;
        } else {
            Main.log("Could not find Enchantment with name: " + enchantmentDO.name);
            return null;
        }

//      Increase enchantment level if necessary and allowed
        while (getValue(enchantmentValue, (enchantmentLevel + 1) * amount) < maxValue && enchantmentLevel < maxEnchantmentLevel) {
            enchantmentLevel++;
        }

//      Increase amount if necessary and allowed
        double maxAmountFactor = Config.getQuantityFactor() * Config.getRewardFactor() * 0.75;
        while (getValue(enchantmentValue, enchantmentLevel * (amount + 1)) < maxValue && amount < enchantmentDO.max * maxAmountFactor) {
            amount += enchantmentDO.step;
        }
        itemValue = getValue(enchantmentValue, enchantmentLevel * amount);

        Material material = Material.ENCHANTED_BOOK;
        item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();

        if (!(itemMeta instanceof EnchantmentStorageMeta)) {
            Main.log("Could not find EnchantmentStorageMeta for item with Material: " + item.getType().name());
            return null;
        }

        ((EnchantmentStorageMeta) itemMeta).addStoredEnchant(enchantment, enchantmentLevel, true);
        item.setItemMeta(itemMeta);

        return new RewardItem(item, itemValue);
    }

    private static RewardItem getPotionReward(DecisionObject potionDO, double maxValue) {

        Random r = new Random();

        Material material = Material.POTION;
        double materialValue;
        ItemStack item;
        double itemValue;
        int amount = potionDO.min;
        PotionType potionType;
        String variant;
        boolean extended = false;
        boolean upgraded = false;

        try {
            potionType = PotionType.valueOf(potionDO.name);
        } catch (Exception e) {
            Main.log("Could not find PotionType: " + potionDO.name);
            return null;
        }


//        Select Random Variant
        variant = (String) potionDO.variants.keySet().toArray()[r.nextInt(potionDO.variants.size())];
        if (variant.equalsIgnoreCase("EX"))
            extended = true;
        else if (variant.equalsIgnoreCase("UP"))
            upgraded = true;

        materialValue = potionDO.variants.get(variant);
        itemValue = getValue(materialValue, amount);

//        If maxValue allows make it a splash potion at random
        if (itemValue + splashPotionValue <= maxValue) {
            if (r.nextDouble() <= splashPotionChance) {
                material = Material.SPLASH_POTION;
                materialValue += splashPotionValue;
            }
        }

//        Increase amount as long as maxValue & DecisionObject.max allow
        double maxAmountFactor = Config.getQuantityFactor() * Config.getRewardFactor() * 0.75;
        while (getValue(materialValue, amount + 1) < maxValue && amount < potionDO.max * maxAmountFactor) {
            amount += potionDO.step;
        }

        itemValue = getValue(materialValue, amount);

        item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null) { System.out.println("e"); }
        if (!(itemMeta instanceof PotionMeta)) {
            Main.log("Could not find PotionData for item with Material: " + item.getType().name());
            return null;
        }

        ((PotionMeta) itemMeta).setBasePotionData(new PotionData(potionType, extended, upgraded));
        item.setItemMeta(itemMeta);

        return new RewardItem(item, itemValue);
    }

    private static double getValue(double materialValue, int amount, double enchantmentValue, int enchantmentLevel) {
        return materialValue * amount + enchantmentValue * enchantmentLevel;
    }

    private static double getValue(double materialValue, int amount) {
        return materialValue * amount;
    }
}
