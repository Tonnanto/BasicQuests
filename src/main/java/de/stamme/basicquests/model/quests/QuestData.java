package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.model.rewards.Reward;
import de.stamme.basicquests.model.rewards.RewardType;
import de.stamme.basicquests.model.wrapper.structure.QuestStructureType;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.Nullable;

public class QuestData implements Serializable {
    private static final long serialVersionUID = -3976762424091379760L;

    // ---------------------------------------------------------------------------------------
    // Quest Attributes & State
    // ---------------------------------------------------------------------------------------

    // ALL QUESTS
    private String questType;
    private int goal;
    private int count;
    private Reward reward;
    private boolean rewardReceived;
    private double value;

    // BREAK_BLOCK - CHOP_WOOD - HARVEST_BLOCK - ENCHANT_ITEM - VILLAGER_TRADE
    private String material;

    // CHOP_WOOD (Log)
    private String materialString;

    // KILL_ENTITY
    private String entity;

    // ENCHANT_ITEM
    private String enchantment;
    private int enchantmentLvl;

    // FIND_STRUCTURE
    private String structure;
    private double radius;

    // FISH_ITEM
    private String option;

    // INCREASE_STAT
    private String statistic;
    private int startValue;

    // ---------------------------------------------------------------------------------------
    // Serialization
    // ---------------------------------------------------------------------------------------

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        if (reward == null) {
            throw new InvalidObjectException("reward must not be null");
        }
        if (questType == null) {
            throw new InvalidObjectException("questType must not be null");
        }
    }

    // ---------------------------------------------------------------------------------------
    // Functionality
    // ---------------------------------------------------------------------------------------

    /**
     * Tries to create a Quest based on attributes
     *
     * @return the created Quest or null
     */
    @Nullable
    public Quest toQuest() {

        Quest quest = null;

        if (questType.equals(QuestType.BREAK_BLOCK.name())) {

            // +++++++++++++++++++++ Converting Log Quests in old Player Data to ChopWoodQuests
            // ++++++++++++++++++++++++
            if (materialString != null && materialString.equalsIgnoreCase("LOG")) {
                quest = new ChopWoodQuest("LOG", goal, reward);

            } else if (material.equalsIgnoreCase(Material.ACACIA_LOG.name()) || material.equalsIgnoreCase(Material.BIRCH_LOG.name())
                    || material.equalsIgnoreCase(Material.DARK_OAK_LOG.name()) || material.equalsIgnoreCase(Material.JUNGLE_LOG.name())
                    || material.equalsIgnoreCase(Material.OAK_LOG.name()) || material.equalsIgnoreCase(Material.SPRUCE_LOG.name())) {

                try {
                    Material mat = Material.valueOf(material);
                    quest = new ChopWoodQuest(mat, goal, reward);

                } catch (Exception exception) {
                    BasicQuestsPlugin.log(Level.SEVERE, String.format("Material '%s' does not exist.", material));
                }
                // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            } else {
                try {
                    Material mat = Material.valueOf(material);
                    quest = new BlockBreakQuest(mat, goal, reward);

                } catch (Exception exception) {
                    BasicQuestsPlugin.log(Level.SEVERE, String.format("Material '%s' does not exist.", material));
                }
            }

        } else if (questType.equals(QuestType.MINE_BLOCK.name())) {
            try {
                Material mat = Material.valueOf(material);
                quest = new MineBlockQuest(mat, goal, reward);

            } catch (Exception exception) {
                BasicQuestsPlugin.log(Level.SEVERE, String.format("Material '%s' does not exist.", material));
            }

        } else if (questType.equals(QuestType.HARVEST_BLOCK.name())) {
            try {
                Material mat = Material.valueOf(material);
                quest = new HarvestBlockQuest(mat, goal, reward);

            } catch (Exception exception) {
                BasicQuestsPlugin.log(Level.SEVERE, String.format("Material '%s' does not exist.", material));
            }

        } else if (questType.equals(QuestType.ENCHANT_ITEM.name())) {
            Enchantment enc = null;
            if (enchantment != null) {
                NamespacedKey enchantmentKey = NamespacedKey.minecraft(enchantment.toLowerCase());
                enc = Registry.ENCHANTMENT.get(enchantmentKey);
            }

            try {
                Material mat = Material.valueOf(material);
                if (enc != null) {
                    quest = new EnchantItemQuest(mat, enc, enchantmentLvl, goal, reward);
                } else {
                    quest = new EnchantItemQuest(mat, goal, reward);
                }
            } catch (Exception exception) {
                BasicQuestsPlugin.log(Level.SEVERE, String.format("Material '%s' does not exist.", material));
            }

        } else if (questType.equals(QuestType.KILL_ENTITY.name())) {
            try {
                EntityType ent = EntityType.valueOf(entity);
                quest = new EntityKillQuest(ent, goal, reward);

            } catch (Exception exception) {
                BasicQuestsPlugin.log(Level.SEVERE, String.format("EntityType '%s' does not exist.", entity));
            }

        } else if (questType.equals(QuestType.GAIN_LEVEL.name())) {
            quest = new GainLevelQuest(goal, reward);

        } else if (questType.equals(QuestType.REACH_LEVEL.name())) {
            quest = new ReachLevelQuest(goal, reward);

        } else if (questType.equals(QuestType.FIND_STRUCTURE.name())) {
            QuestStructureType structureType = QuestStructureType.fromString(structure);
            if (structureType != null) {
                quest = new FindStructureQuest(structureType, radius, goal, reward);
            }

        } else if (questType.equals(QuestType.CHOP_WOOD.name())) {
            if (materialString != null && materialString.equalsIgnoreCase("LOG")) {
                quest = new ChopWoodQuest("LOG", goal, reward);

            } else {
                try {
                    Material mat = Material.valueOf(material);
                    quest = new ChopWoodQuest(mat, goal, reward);

                } catch (Exception exception) {
                    BasicQuestsPlugin.log(Level.SEVERE, String.format("Material '%s' does not exist.", material));
                }
            }

        } else if (questType.equals(QuestType.VILLAGER_TRADE.name())) {
            NamespacedKey villagerProfessionKey = NamespacedKey.minecraft(material.toLowerCase());
            Villager.Profession profession = Registry.VILLAGER_PROFESSION.get(villagerProfessionKey);
            quest = new VillagerTradeQuest(profession, goal, reward);

        } else if (questType.equals(QuestType.FISH_ITEM.name())) {

            FishItemQuest.Option fishOption = FishItemQuest.Option.valueOf(option);

            if (fishOption != FishItemQuest.Option.MATERIAL) {
                quest = new FishItemQuest(fishOption, goal, reward);

            } else {
                try {
                    Material mat = Material.valueOf(material);
                    quest = new FishItemQuest(mat, goal, reward);

                } catch (Exception exception) {
                    BasicQuestsPlugin.log(Level.SEVERE, String.format("Material '%s' does not exist.", material));
                }
            }
        } else if (questType.equals(QuestType.INCREASE_STAT.name())) {
            Statistic statistic = Statistic.valueOf(this.statistic);
            quest = new IncreaseStatQuest(statistic, startValue, goal, reward);
        }

        // if the quest was successfully initialized -> adjust count and value
        if (quest != null) {
            quest.setCount(count);
            quest.setValue(value);
        }

        return quest;
    }

    // ---------------------------------------------------------------------------------------
    // Getter & Setter
    // ---------------------------------------------------------------------------------------

    /**
     * @return whether this quest is invalid and needs to be regenerated
     */
    public boolean isInvalid() {

        boolean hasInvalidMoneyReward = (getReward().getRewardType() == RewardType.MONEY || getReward().getMoney().compareTo(BigDecimal.ZERO) > 0)
                && !Config.moneyRewards();

        boolean hasInvalidXpReward = (getReward().getRewardType() == RewardType.XP || getReward().getXp() > 0) && !Config.xpRewards();

        boolean hasInvalidItemReward = (getReward().getRewardType() == RewardType.ITEM
                || (getReward().getRewardItems() != null && !getReward().getRewardItems().isEmpty())) && !Config.itemRewards();

        return (hasInvalidMoneyReward || hasInvalidXpReward || hasInvalidItemReward);
    }

    public String toString() {
        return String.format("Type: %s, goal: %s, count %s, reward %s, mat: %s, ent: %s, enc: %s", questType, goal, count, reward.getMoney(), material, entity,
                enchantment);
    }

    public String getQuestType() {
        return questType;
    }

    public Reward getReward() {
        return reward;
    }

    public void setQuestType(String questType) {
        this.questType = questType;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    public boolean isRewardReceived() {
        return rewardReceived;
    }

    public void setRewardReceived(boolean rewardReceived) {
        this.rewardReceived = rewardReceived;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getMaterialString() {
        return materialString;
    }

    public void setMaterialString(String materialString) {
        this.materialString = materialString;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEnchantment() {
        return enchantment;
    }

    public void setEnchantment(String enchantment) {
        this.enchantment = enchantment;
    }

    public int getEnchantmentLvl() {
        return enchantmentLvl;
    }

    public void setEnchantmentLvl(int enchantmentLvl) {
        this.enchantmentLvl = enchantmentLvl;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getStatistic() {
        return statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    public int getStartValue() {
        return startValue;
    }

    public void setStartValue(int startValue) {
        this.startValue = startValue;
    }
}
