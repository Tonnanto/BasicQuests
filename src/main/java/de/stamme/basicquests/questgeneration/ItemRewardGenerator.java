package de.stamme.basicquests.questgeneration;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.model.generation.GenerationConfig;
import de.stamme.basicquests.model.generation.GenerationOption;
import de.stamme.basicquests.model.rewards.ItemRewardType;
import de.stamme.basicquests.model.rewards.RewardItem;
import de.stamme.basicquests.util.GenerationFileService;
import de.stamme.basicquests.model.quests.QuestType;
import de.stamme.basicquests.model.rewards.Reward;
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

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ItemRewardGenerator {

    private static final double questTypeWeightFactor = 2;
    private static final double lowestMaxValue = 200;
    private static final double splashPotionValue = 32;
    private static final double splashPotionChance = 0.5;
    private static final double rewardAlreadyInQuestsFactor = 0.01;

    public static GenerationOption decide(List<GenerationOption> options, QuestType questType, double maxValue, List<String> rewardsInPlayerQuests) {

        for (GenerationOption option : options) {
            // Remove options where minValue > maxRewardValue
            double minValue;
            if (option.getVariants() != null) {
                Optional<Map.Entry<String, Double>> lowestMat = option.getVariants().entrySet().stream().min(Map.Entry.comparingByValue());
                minValue = option.getMin() * (lowestMat.isPresent() ? lowestMat.get().getValue() : option.getValue());
            } else {
                minValue = option.getMin() * option.getValue();
            }

            if (minValue > maxValue && minValue > lowestMaxValue) {
                option.setWeight(0);

            // Adjust DecisionObjects weight if the Material is already in a Reward for the Player
            } else if (rewardsInPlayerQuests != null && rewardsInPlayerQuests.contains(option.getName())) {
                option.setWeight(option.getWeight() * rewardAlreadyInQuestsFactor);

            // Adjust DecisionObjects weight if the QuestType matches
            } else if (option.getQuestTypes() != null && questType != null) {
                for (String questTypeString : option.getQuestTypes()) {
                    if (questTypeString.equalsIgnoreCase(questType.name())) {
                        option.setWeight(option.getWeight() * questTypeWeightFactor);
                        break;
                    }
                }
            }
        }

        return QuestGenerator.getInstance().decide(options);
    }

    public static Reward generate(QuestType questType, double questValue, List<String> rewardsInPlayerQuests) {

        // Choose a random reward
        // if reward.value > quest.value * 1.5 -> decrease rewards value or choose new reward
        // if reward.value < quest.value * 0.8 -> increase it's value or add another item to the reward

        double minValue = questValue * 0.8;
        double maxValue = questValue * 1.5;

        GenerationConfig toolRewardsList = GenerationFileService.getInstance().getConfigForItemRewardType(ItemRewardType.TOOL_REWARDS);
        GenerationConfig armorRewardsList = GenerationFileService.getInstance().getConfigForItemRewardType(ItemRewardType.ARMOR_REWARDS);
        GenerationConfig enchantmentRewardsList = GenerationFileService.getInstance().getConfigForItemRewardType(ItemRewardType.ENCHANTMENT_REWARDS);
        GenerationConfig resourceRewardsList = GenerationFileService.getInstance().getConfigForItemRewardType(ItemRewardType.RESOURCE_REWARDS);
        GenerationConfig foodRewardsList = GenerationFileService.getInstance().getConfigForItemRewardType(ItemRewardType.FOOD_REWARDS);
        GenerationConfig potionRewardsList = GenerationFileService.getInstance().getConfigForItemRewardType(ItemRewardType.POTION_REWARDS);
        GenerationConfig otherItemRewardsList = GenerationFileService.getInstance().getConfigForItemRewardType(ItemRewardType.OTHER_ITEM_REWARDS);

        assert toolRewardsList.getOptions() != null;
        assert armorRewardsList.getOptions() != null;
        assert enchantmentRewardsList.getOptions() != null;
        assert resourceRewardsList.getOptions() != null;
        assert foodRewardsList.getOptions() != null;
        assert potionRewardsList.getOptions() != null;
        assert otherItemRewardsList.getOptions() != null;

        List<GenerationOption> generationOptions = new ArrayList<>();
        generationOptions.addAll(toolRewardsList.getOptions());
        generationOptions.addAll(armorRewardsList.getOptions());
        generationOptions.addAll(enchantmentRewardsList.getOptions());
        generationOptions.addAll(resourceRewardsList.getOptions());
        generationOptions.addAll(foodRewardsList.getOptions());
        generationOptions.addAll(potionRewardsList.getOptions());
        generationOptions.addAll(otherItemRewardsList.getOptions());

        List<RewardItem> items = new ArrayList<>();
        List<String> materialNames = new ArrayList<>();
        double rewardValue = 0;

        do {
            double minItemValue = minValue - rewardValue;
            double maxItemValue = maxValue - rewardValue;

            GenerationOption materialDO = decide(generationOptions, questType, maxValue, rewardsInPlayerQuests);
            if (materialDO == null) break;

            RewardItem rewardItem;

            if (toolRewardsList.getOptions().contains(materialDO) || armorRewardsList.getOptions().contains(materialDO)) {
                rewardItem = getToolArmorReward(materialDO, minItemValue, maxItemValue);

            } else if (enchantmentRewardsList.getOptions().contains(materialDO)) {
                rewardItem = getEnchantmentReward(materialDO, maxItemValue);

            } else if (potionRewardsList.getOptions().contains(materialDO)) {
                rewardItem = getPotionReward(materialDO, maxItemValue);

            } else {
                rewardItem = getReward(materialDO, maxItemValue);
            }

            // Prevent this Material from reappearing in this reward
            materialDO.setWeight(0);

            if (rewardItem == null) {
                continue;
            }

            rewardValue += rewardItem.value;
            items.add(rewardItem);
            materialNames.add(materialDO.getName());


        } while (rewardValue < minValue);

        return new Reward(new ArrayList<>(items.stream().sorted().map(x -> x.item).collect(Collectors.toList())), materialNames);
    }

    private static RewardItem getReward(GenerationOption materialOption, double maxValue) {

        ItemStack item;
        double itemValue;

        Material material;
        int amount = (materialOption.getMin() > 0) ? materialOption.getMin() : 1;

        double maxAmountFactor = Config.getQuantityFactor() * Config.getRewardFactor() * 0.75;
        while (getValue(materialOption.getValue(), amount + materialOption.getStep()) < maxValue && amount < materialOption.getMax() * maxAmountFactor) {
            amount += materialOption.getStep();
        }
        itemValue = getValue(materialOption.getValue(), amount);

        material = Material.getMaterial(materialOption.getName());

        if (material == null) {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not find Material with name: " + materialOption.getName());
            return null;
        }

        item = new ItemStack(material, amount);

        return new RewardItem(item, itemValue);
    }

    private static RewardItem getToolArmorReward(GenerationOption materialOption, double minValue, double maxValue) {

        ItemStack item;
        double itemValue;

        String materialString = "";
        double materialValue;
        int amount = (materialOption.getMin() > 0) ? materialOption.getMin() : 1;

        GenerationOption enchantmentDO;
        Enchantment enchantment = null;
        double enchantmentValue = 0;
        int enchantmentLevel = 1;
        int maxEnchantmentLevel = 1;

        // Choose lowest Material when available for the selected item
        if (materialOption.getVariants() != null) {
            Optional<Map.Entry<String, Double>> lowestMat = materialOption.getVariants().entrySet().stream().min(Map.Entry.comparingByValue());
            materialString = lowestMat.isPresent() ? lowestMat.get().getKey() : "";
            materialValue = lowestMat.isPresent() ? lowestMat.get().getValue() : materialOption.getValue();
        } else {
            materialValue = materialOption.getValue();
        }

        //  Choose Enchantment when available for the selected item
        if (materialOption.getOptions() != null) {
            enchantmentDO = decide(materialOption.getOptions(), null, maxValue - materialValue, null);
            assert enchantmentDO != null;

            enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantmentDO.getName().toLowerCase()));

            if (enchantment != null) {
                maxEnchantmentLevel = enchantment.getMaxLevel();
                if (maxEnchantmentLevel > 1) {
                    Random r = new Random();
                    enchantmentLevel = r.nextInt(enchantment.getMaxLevel() - 1) + 1;
                }

                enchantmentValue = enchantmentDO.getValue();
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

                    newMaterialValue = materialOption.getVariants().get(newMaterialString);
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

        // Increase amount if necessary and allowed
        double maxAmountFactor = Config.getQuantityFactor() * Config.getRewardFactor() * 0.75;
        while (getValue(materialValue, amount + materialOption.getStep(), enchantmentValue, enchantmentLevel) < maxValue && amount < materialOption.getMax() * maxAmountFactor) {
            amount += materialOption.getStep();
        }
        itemValue = getValue(materialValue, amount, enchantmentValue, enchantmentLevel);

        Material material;

        if (!materialString.isEmpty())
            materialString += "_" + materialOption.getName();
        else
            materialString = materialOption.getName();

        material = Material.getMaterial(materialString);

        if (material == null) {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not find Material with name: " + materialString);
            return null;
        }

        item = new ItemStack(material, amount);

        if (enchantment != null)
            item.addEnchantment(enchantment, enchantmentLevel);

        return new RewardItem(item, itemValue);
    }

    private static RewardItem getEnchantmentReward(GenerationOption enchantmentOption, double maxValue) {

        ItemStack item;
        double itemValue;

        int amount = (enchantmentOption.getMin() > 0) ? enchantmentOption.getMin() : 1;

        Enchantment enchantment;
        double enchantmentValue;
        int enchantmentLevel = 1;
        int maxEnchantmentLevel;

        enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantmentOption.getName().toLowerCase()));

        if (enchantment != null) {
            maxEnchantmentLevel = enchantment.getMaxLevel();
            if (maxEnchantmentLevel > 1) {
                Random r = new Random();
                enchantmentLevel = r.nextInt(enchantment.getMaxLevel()) + 1;
            }

            enchantmentValue = enchantmentOption.getValue();
        } else {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not find Enchantment with name: " + enchantmentOption.getName());
            return null;
        }

        // Increase enchantment level if necessary and allowed
        while (getValue(enchantmentValue, (enchantmentLevel + 1) * amount) < maxValue && enchantmentLevel < maxEnchantmentLevel) {
            enchantmentLevel++;
        }

        // Increase amount if necessary and allowed
        double maxAmountFactor = Config.getQuantityFactor() * Config.getRewardFactor() * 0.75;
        while (getValue(enchantmentValue, enchantmentLevel * (amount + 1)) < maxValue && amount < enchantmentOption.getMax() * maxAmountFactor) {
            amount += enchantmentOption.getStep();
        }
        itemValue = getValue(enchantmentValue, enchantmentLevel * amount);

        Material material = Material.ENCHANTED_BOOK;
        item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();

        if (!(itemMeta instanceof EnchantmentStorageMeta)) {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not find EnchantmentStorageMeta for item with Material: " + item.getType().name());
            return null;
        }

        ((EnchantmentStorageMeta) itemMeta).addStoredEnchant(enchantment, enchantmentLevel, true);
        item.setItemMeta(itemMeta);

        return new RewardItem(item, itemValue);
    }

    private static RewardItem getPotionReward(GenerationOption potionOption, double maxValue) {

        Random r = new Random();

        Material material = Material.POTION;
        double materialValue;
        ItemStack item;
        double itemValue;
        int amount = potionOption.getMin();
        PotionType potionType;
        String variant;
        boolean extended = false;
        boolean upgraded = false;

        try {
            potionType = PotionType.valueOf(potionOption.getName());
        } catch (Exception e) {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not find PotionType: " + potionOption.getName());
            return null;
        }


        // Select Random Variant
        variant = (String) potionOption.getVariants().keySet().toArray()[r.nextInt(potionOption.getVariants().size())];
        if (variant.equalsIgnoreCase("EX"))
            extended = true;
        else if (variant.equalsIgnoreCase("UP"))
            upgraded = true;

        materialValue = potionOption.getVariants().get(variant);
        itemValue = getValue(materialValue, amount);

        // If maxValue allows make it a splash potion at random
        if (itemValue + splashPotionValue <= maxValue) {
            if (r.nextDouble() <= splashPotionChance) {
                material = Material.SPLASH_POTION;
                materialValue += splashPotionValue;
            }
        }

        // Increase amount as long as maxValue & DecisionObject.max allow
        double maxAmountFactor = Config.getQuantityFactor() * Config.getRewardFactor() * 0.75;
        while (getValue(materialValue, amount + 1) < maxValue && amount < potionOption.getMax() * maxAmountFactor) {
            amount += potionOption.getStep();
        }

        itemValue = getValue(materialValue, amount);

        item = new ItemStack(material, amount);
        item.hasItemMeta();
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof PotionMeta)) {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not find PotionData for item with Material: " + item.getType().name());
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
