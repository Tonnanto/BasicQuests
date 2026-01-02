package de.stamme.basicquests.questgeneration;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.model.generation.GenerationConfig;
import de.stamme.basicquests.model.generation.GenerationOption;
import de.stamme.basicquests.model.generation.QuestGenerationException;
import de.stamme.basicquests.model.rewards.Reward;
import de.stamme.basicquests.model.rewards.RewardType;
import de.stamme.basicquests.util.GenerationFileService;
import de.stamme.basicquests.model.wrapper.structure.QuestStructureType;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.*;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class QuestGenerator {

    private static QuestGenerator instance;

    public static QuestGenerator getInstance() {
        if (instance == null)
            instance = new QuestGenerator();
        return instance;
    }

    private QuestGenerator() {
    }

    /**
     * randomly decides for an object based on the given weight
     *
     * @param objects objects to decide from
     * @return the decided object
     */
    public GenerationOption decide(List<GenerationOption> objects) {
        double x = 0;
        double tot = objects.stream()
            .map(GenerationOption::getWeight)
            .mapToDouble(Double::doubleValue)
            .sum();

        Random r = new Random();
        double target = tot * r.nextDouble();

        for (GenerationOption obj : objects) {
            x += obj.getWeight();
            if (x >= target) {
                return obj;
            }
        }

        return null;
    }

    public GenerationOption decide(List<GenerationOption> options, QuestPlayer questPlayer) {

        for (GenerationOption option : options) {

            // set Options weight to 0 if a required advancement has not been made (eg. "story/mine_diamond")
            if (option.getAdvancements() != null) {
                for (String key : option.getAdvancements()) {
                    key = key.replace(".", "/");
                    Advancement adv = Bukkit.getAdvancement(NamespacedKey.minecraft(key));
                    if (adv != null && !questPlayer.getPlayer().getAdvancementProgress(adv).isDone()) {
                        option.setWeight(0);
                    }
                }
            }

            // Reduce weight of Options that are already in the players quests
            if (questPlayer.getQuests() != null) {
                for (Quest quest : questPlayer.getQuests()) {
                    for (String name : quest.getOptionNames()) {
                        if (option.getName().equalsIgnoreCase(name)) {
                            option.setWeight(option.getWeight() * Config.duplicateQuestChance());
                            break;
                        }
                    }
                }
            }
        }

        return decide(options);
    }

    public Quest generate(QuestPlayer questPlayer) throws QuestGenerationException {

        double reward_factor = Config.getRewardFactor();
        double amount_factor = Config.getQuantityFactor();

        if (Config.increaseAmountByPlaytime())
            amount_factor *= getPlaytimeAmountFactor(questPlayer.getPlayer());

        Quest quest = null;
        GenerationConfig generationConfig = GenerationFileService.getInstance().getQuestTypeGenerationConfig();

        assert generationConfig.getOptions() != null;
        GenerationOption questTypeOption = decide(generationConfig.getOptions(), questPlayer);

        QuestType questType;

        try {
            questType = QuestType.valueOf(questTypeOption.getName());
        } catch (IllegalArgumentException exception) {
            // QuestType was not found
            throw new QuestGenerationException(String.format("QuestType '%s' does not exist.", questTypeOption.getName()));
        }

        switch (questType) {
            case BREAK_BLOCK:
                quest = generateBreakBlockQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
                break;
            case MINE_BLOCK:
                quest = generateMineBlockQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
                break;
            case HARVEST_BLOCK:
                quest = generateHarvestBlockQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
                break;
            case ENCHANT_ITEM:
                quest = generateEnchantItemQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
                break;
            case KILL_ENTITY:
                quest = generateKillEntityQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
                break;
            case GAIN_LEVEL:
                quest = generateGainLevelQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
                break;
            case REACH_LEVEL:
                quest = generateReachLevelQuest(questPlayer, questTypeOption.getValue() * reward_factor);
                break;
            case FIND_STRUCTURE:
                quest = generateFindStructureQuest(questPlayer, questTypeOption.getValue() * reward_factor);
                break;
            case CHOP_WOOD:
                quest = generateChopWoodQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
                break;
            case VILLAGER_TRADE:
                quest = generateVillagerTradeQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
                break;
            case FISH_ITEM:
                quest = generateFishItemQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
                break;
            case INCREASE_STAT:
                quest = generateIncreaseStatQuest(questPlayer, questTypeOption.getValue() * reward_factor, amount_factor);
                break;
        }

        // Prevent null quests
        if (quest == null) quest = generate(questPlayer);

        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Break Block Quest
    // ---------------------------------------------------------------------------------------

    Quest generateBreakBlockQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.BREAK_BLOCK);

        assert generationConfig.getOptions() != null;
        GenerationOption materialOption = decide(generationConfig.getOptions(), questPlayer);

        Material materialToBreak = Material.getMaterial(materialOption.getName());
        if (materialToBreak == null) {
            BasicQuestsPlugin.log(Level.INFO, String.format("Material '%s' does not exist in this version.", materialOption.getName()));
            return generate(questPlayer);
        }

        int amountToBreak = generateAmount(materialOption, generationConfig, amount_factor);

        double value = materialOption.getValue(amountToBreak) * reward_factor;
        Reward reward = generateReward(QuestType.BREAK_BLOCK, value, questPlayer);

        Quest quest = new BlockBreakQuest(materialToBreak, amountToBreak, reward);
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Mine Block Quest
    // ---------------------------------------------------------------------------------------

    Quest generateMineBlockQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.MINE_BLOCK);

        assert generationConfig.getOptions() != null;
        GenerationOption materialOption = decide(generationConfig.getOptions(), questPlayer);

        Material materialToMine = Material.getMaterial(materialOption.getName());
        if (materialToMine == null) {
            BasicQuestsPlugin.log(Level.INFO, String.format("Material '%s' does not exist in this version.", materialOption.getName()));
            return generate(questPlayer);
        }

        int amountToMine = generateAmount(materialOption, generationConfig, amount_factor);

        double value = materialOption.getValue(amountToMine) * reward_factor;
        Reward reward = generateReward(QuestType.MINE_BLOCK, value, questPlayer);

        Quest quest = new MineBlockQuest(materialToMine, amountToMine, reward);
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Harvest Block Quest
    // ---------------------------------------------------------------------------------------

    Quest generateHarvestBlockQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.HARVEST_BLOCK);

        assert generationConfig.getOptions() != null;
        GenerationOption materialOption = decide(generationConfig.getOptions(), questPlayer);

        Material materialToHarvest = Material.getMaterial(materialOption.getName());
        if (materialToHarvest == null) {
            BasicQuestsPlugin.log(Level.INFO, String.format("Material '%s' does not exist in this version.", materialOption.getName()));
            return generate(questPlayer);
        }

        int amountToHarvest = generateAmount(materialOption, generationConfig, amount_factor);

        double value = materialOption.getValue(amountToHarvest) * reward_factor;
        Reward reward = generateReward(QuestType.HARVEST_BLOCK, value, questPlayer);

        Quest quest = new HarvestBlockQuest(materialToHarvest, amountToHarvest, reward);
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Chop Wood Quest
    // ---------------------------------------------------------------------------------------

    Quest generateChopWoodQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.CHOP_WOOD);

        assert generationConfig.getOptions() != null;
        GenerationOption woodOption = decide(generationConfig.getOptions(), questPlayer);
        Material woodToChop = null;

        if (!woodOption.getName().equalsIgnoreCase("LOG")) {
            woodToChop = Material.getMaterial(woodOption.getName());
            if (woodToChop == null) {
                BasicQuestsPlugin.log(Level.INFO, String.format("Material '%s' does not exist in this version.", woodOption.getName()));
                return generate(questPlayer);
            }
        }

        // Check if Material was found   OR   material.name == LOG
        if (woodToChop == null && !woodOption.getName().equalsIgnoreCase("LOG")) {
            BasicQuestsPlugin.log(Level.INFO, String.format("Material '%s' does not exist in this version.", woodOption.getName()));
            return generate(questPlayer);
        }

        int amountToChop = generateAmount(woodOption, generationConfig, amount_factor);
        double value = woodOption.getValue(amountToChop) * reward_factor;
        Reward reward = generateReward(QuestType.CHOP_WOOD, value, questPlayer);

        Quest quest;
        if (woodOption.getName().equalsIgnoreCase("LOG")) { // General Log quest that accepts all kind of logs
            quest = new ChopWoodQuest(woodOption.getName(), amountToChop, reward);
        } else {
            quest = new ChopWoodQuest(woodToChop, amountToChop, reward);
        }
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Kill Entity Quest
    // ---------------------------------------------------------------------------------------

    Quest generateKillEntityQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.KILL_ENTITY);

        assert generationConfig.getOptions() != null;
        GenerationOption entityOption = decide(generationConfig.getOptions(), questPlayer);
        EntityType entityToKill;

        try {
            entityToKill = EntityType.valueOf(entityOption.getName());
        } catch (IllegalArgumentException exception) {
            // If Entity was not found
            BasicQuestsPlugin.log(Level.INFO, String.format("Entity '%s' does not exist in this version.", entityOption.getName()));
            return generate(questPlayer);
        }

        int amountToKill = generateAmount(entityOption, generationConfig, amount_factor);

        double value = entityOption.getValue(amountToKill) * reward_factor;
        Reward reward = generateReward(QuestType.KILL_ENTITY, value, questPlayer);

        Quest quest = new EntityKillQuest(entityToKill, amountToKill, reward);
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Enchant Item Quest
    // ---------------------------------------------------------------------------------------

    Quest generateEnchantItemQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.ENCHANT_ITEM);

        assert generationConfig.getOptions() != null;
        GenerationOption materialOption = decide(generationConfig.getOptions(), questPlayer);

        Material itemToEnchant = Material.getMaterial(materialOption.getName());
        if (itemToEnchant == null) {
            BasicQuestsPlugin.log(Level.INFO, String.format("Material '%s' does not exist in this version.", materialOption.getName()));
            return generate(questPlayer);
        }

        int amountToEnchant = generateAmount(materialOption, generationConfig, amount_factor);


        if (materialOption.getOptions() != null) { // When Enchantments are available

            GenerationOption enchantmentOption = decide(materialOption.getOptions());
            assert enchantmentOption != null;

            NamespacedKey enchantmentKey = NamespacedKey.minecraft(enchantmentOption.getName().toLowerCase());
            Enchantment enchantment = Registry.ENCHANTMENT.get(enchantmentKey);

            if (enchantment != null) {
                int enchantmentLevel = 1;
                if (enchantment.getMaxLevel() > 2) {
                    Random r = new Random();
                    enchantmentLevel = r.nextInt(enchantment.getMaxLevel() - 1) + 1;
                }
                double value = materialOption.getValue(amountToEnchant) * enchantmentOption.getValue(enchantmentLevel) * reward_factor;
                Reward reward = generateReward(QuestType.ENCHANT_ITEM, value, questPlayer);

                Quest quest = new EnchantItemQuest(itemToEnchant, enchantment, enchantmentLevel, amountToEnchant, reward);
                quest.setValue(value);
                return quest;
            }
        }

        // No Enchantment requirements (p.E. Shield) or no enchantment found by key
        double value = reward_factor * materialOption.getValue(amountToEnchant);
        Reward reward = generateReward(QuestType.ENCHANT_ITEM, value, questPlayer);

        Quest quest = new EnchantItemQuest(itemToEnchant, amountToEnchant, reward);
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Reach Level Quest
    // ---------------------------------------------------------------------------------------

    Quest generateReachLevelQuest(QuestPlayer questPlayer, double reward_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.REACH_LEVEL);

        int minReach = generationConfig.getDefault_min();
        int maxReach = generationConfig.getDefault_max();
        int stepReach = generationConfig.getDefault_step();
        double value_per_xp = generationConfig.getValue_per_unit();

        int playerLevel = questPlayer.getPlayer().getLevel();

        if (playerLevel > minReach - 2) {
            minReach = playerLevel + 2;
        } // Raise minimum level to reach if player.level is already higher

        if (minReach < maxReach) {

            int amountToReach = generateAmount(minReach, maxReach, stepReach, 1.0);

            double xpRequired = xpToReachLevel(amountToReach) - xpToReachLevel(playerLevel);

            double value = reward_factor * value_per_xp * xpRequired;
            Reward reward = generateReward(QuestType.REACH_LEVEL, value, questPlayer);

            Quest quest = new ReachLevelQuest(questPlayer, amountToReach, reward);
            quest.setValue(value);
            return quest;

        } else { // Generate a new Quest if player.level is higher than maximum level to reach
            return generate(questPlayer);
        }
    }


    // ---------------------------------------------------------------------------------------
    // Gain Level Quest
    // ---------------------------------------------------------------------------------------

    Quest generateGainLevelQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.GAIN_LEVEL);

        int minGain = generationConfig.getDefault_min();
        int maxGain = generationConfig.getDefault_max();
        int stepGain = generationConfig.getDefault_step();
        double value_per_level = generationConfig.getValue_per_unit();

        int amountToGain = generateAmount(minGain, maxGain, stepGain, amount_factor);

        double value = reward_factor * value_per_level * amountToGain;
        Reward reward = generateReward(QuestType.GAIN_LEVEL, value, questPlayer);

        Quest quest = new GainLevelQuest(amountToGain, reward);
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Find Structure Quest
    // ---------------------------------------------------------------------------------------

    Quest generateFindStructureQuest(QuestPlayer questPlayer, double reward_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.FIND_STRUCTURE);

        assert generationConfig.getOptions() != null;
        GenerationOption structureOption = decide(generationConfig.getOptions(), questPlayer);
        QuestStructureType structureToFind = QuestStructureType.fromString(structureOption.getName());

        // Check if Structure was found
        if (structureToFind == null) {
            BasicQuestsPlugin.log(Level.INFO, String.format("QuestStructureType '%s' is not available in this version.", structureOption.getName()));
            return generate(questPlayer);
        }

        double value = reward_factor * structureOption.getValue(1);
        Reward reward = generateReward(QuestType.FIND_STRUCTURE, value, questPlayer);

        Quest quest = new FindStructureQuest(structureToFind, structureOption.getRadius(), 1, reward);
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Villager Trade Quest
    // ---------------------------------------------------------------------------------------

    Quest generateVillagerTradeQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.VILLAGER_TRADE);

        assert generationConfig.getOptions() != null;
        GenerationOption professionOption = decide(generationConfig.getOptions(), questPlayer);
        Villager.Profession professionToTradeWith;

        // Check if Profession was found
        try {
            professionToTradeWith = Registry.VILLAGER_PROFESSION.get(NamespacedKey.minecraft(professionOption.getName().toLowerCase()));
        } catch (IllegalArgumentException exception) {
            // If Profession was not found
            BasicQuestsPlugin.log(Level.SEVERE, String.format("Profession '%s' does not exist.", professionOption.getName()));
            return generate(questPlayer);
        }

        int numberOfTrades = generateAmount(professionOption, generationConfig, amount_factor);

        double value = professionOption.getValue(numberOfTrades) * reward_factor;
        Reward reward = generateReward(QuestType.VILLAGER_TRADE, value, questPlayer);

        Quest quest = new VillagerTradeQuest(professionToTradeWith, numberOfTrades, reward);
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Fish Item Quest
    // ---------------------------------------------------------------------------------------

    Quest generateFishItemQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.FISH_ITEM);

        assert generationConfig.getOptions() != null;
        GenerationOption materialOption = decide(generationConfig.getOptions(), questPlayer);
        FishItemQuest.Option fishingOption = null;

        // Check if Material exists
        Material materialToFish = Material.getMaterial(materialOption.getName());
        if (materialToFish == null) {

            // Check if alternative Fishing Option exists
            try {
                fishingOption = FishItemQuest.Option.valueOf(materialOption.getName());
            } catch (IllegalArgumentException _exception) {
                BasicQuestsPlugin.log(Level.INFO, String.format("Material '%s' does not exist in this version.", materialOption.getName()));
                return generate(questPlayer);
            }
        }

        int amountToFish = generateAmount(materialOption, generationConfig, amount_factor);

        double value = materialOption.getValue(amountToFish) * reward_factor;
        Reward reward = generateReward(QuestType.FISH_ITEM, value, questPlayer);

        Quest quest;
        if (materialToFish != null) {
            quest = new FishItemQuest(materialToFish, amountToFish, reward);
        } else {
            quest = new FishItemQuest(fishingOption, amountToFish, reward);
        }
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Increase Stat Quest
    // ---------------------------------------------------------------------------------------

    Quest generateIncreaseStatQuest(QuestPlayer questPlayer, double reward_factor, double amount_factor) throws QuestGenerationException {
        GenerationConfig generationConfig = GenerationFileService.getInstance().getConfigForQuestType(QuestType.INCREASE_STAT);

        assert generationConfig.getOptions() != null;
        GenerationOption statisticOption = decide(generationConfig.getOptions(), questPlayer);
        Statistic statisticToIncrease;

        // Check if Statistic exists
        try {
            statisticToIncrease = Statistic.valueOf(statisticOption.getName());
        } catch (IllegalArgumentException exception) {
            // If Statistic was not found
            BasicQuestsPlugin.log(Level.SEVERE, String.format("Statistic '%s' does not exist.", statisticOption.getName()));
            return generate(questPlayer);
        }

        int amountToIncrease = generateAmount(statisticOption, generationConfig, amount_factor);

        double value = statisticOption.getValue(amountToIncrease) * reward_factor;
        Reward reward = generateReward(QuestType.INCREASE_STAT, value, questPlayer);

        // Determine initial statistic progress
        int startValue;
        if (statisticToIncrease == Statistic.WALK_ONE_CM || statisticToIncrease == Statistic.SPRINT_ONE_CM) {
            startValue = questPlayer.getPlayer().getStatistic(Statistic.WALK_ONE_CM) +
                questPlayer.getPlayer().getStatistic(Statistic.SPRINT_ONE_CM);
        } else {
            startValue = questPlayer.getPlayer().getStatistic(statisticToIncrease);
        }

        Quest quest = new IncreaseStatQuest(statisticToIncrease, startValue, amountToIncrease, reward);
        quest.setValue(value);
        return quest;
    }


    // ---------------------------------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------------------------------

    /**
     * Returns an appropriate amount for a given DecisionObject. p.e: The amount of coal ores to break in a quest
     *
     * @param generationOption the GenerationOption to generate an amount for
     * @param generationConfig the GenerationConfig that contains default values for min and max amount values
     * @param multiplier       the multiplier for the amount
     * @return the generated amount
     */
    public int generateAmount(GenerationOption generationOption, GenerationConfig generationConfig, double multiplier) {

        int min, max, step;

        if (generationOption.getMax() > 0 && generationOption.getStep() > 0) {
            min = generationOption.getMin();
            max = generationOption.getMax();
            step = generationOption.getStep();
        } else {
            min = generationConfig.getDefault_min();
            max = generationConfig.getDefault_max();
            step = generationConfig.getDefault_step();
        }

        if (max <= 1 || step < 1) {
            return 1;
        }

        return generateAmount(min, max, step, multiplier);
    }

    /**
     * calculates a random amount based on the given values
     *
     * @param min        minimum amount
     * @param max        maximum amount
     * @param step       the number the amount should be dividable by
     * @param multiplier multiplier for the amount
     * @return the generated amount
     */
    public int generateAmount(int min, int max, int step, double multiplier) {
        Random r = new Random();
        double value = r.nextInt(max - min) + min;
        value = (value * multiplier) - (value * multiplier) % step;
        if (value < min) {
            value += step;
        }
        return (int) value;
    }

    /**
     * calculates the amount factor by a players playtime based on values in config
     *
     * @param player the player to calculate the factor for
     * @return the factor based on the players playtime
     */
    protected double getPlaytimeAmountFactor(Player player) {
        FileConfiguration config = BasicQuestsPlugin.getPlugin().getConfig();

        int ticks_played = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int hours_played = ticks_played / 20 / 60 / 60;

        double start_factor = config.getDouble("start-factor");
        double max_factor = config.getDouble("max-factor");
        double max_amount_hours = config.getDouble("max-amount-hours");

        double factor = start_factor + (max_factor - start_factor) * ((double) hours_played / max_amount_hours);
        return Math.min(max_factor, factor);
    }

    private double xpToReachLevel(int lvl) {
        if (lvl <= 15) {
            return Math.pow(lvl, 2) + 6 * lvl;
        } else if (lvl <= 31) {
            return 2.5 * Math.pow(lvl, 2) - 40.5 * lvl + 360;
        } else {
            return 4.5 * Math.pow(lvl, 2) - 162.5 * lvl + 2220;
        }
    }

    private Reward generateReward(QuestType questType, double questValue, QuestPlayer questPlayer) {

        Random r = new Random();
        List<RewardType> list = new ArrayList<>();

        if (Config.moneyRewards() && BasicQuestsPlugin.getEconomy() != null)
            list.add(RewardType.MONEY);

        if (Config.xpRewards())
            list.add(RewardType.XP);

        if (Config.itemRewards() || list.isEmpty())
            list.add(RewardType.ITEM);

        switch (list.get(r.nextInt(list.size()))) {
            case ITEM:
                List<String> materialsInRewards = new ArrayList<>();
                questPlayer.getQuests().stream().map(Quest::getReward).filter(x -> x.getMaterialNames() != null).forEach(x -> materialsInRewards.addAll(x.getMaterialNames()));
                return ItemRewardGenerator.generate(questType, questValue, materialsInRewards);

            case MONEY:
                return new Reward(new BigDecimal(Math.round(questValue * Config.getMoneyFactor())));

            case XP:
                return new Reward((int) (questValue * 0.6));
        }

        return new Reward();
    }
}

