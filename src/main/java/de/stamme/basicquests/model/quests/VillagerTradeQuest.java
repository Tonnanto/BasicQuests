package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.util.StringFormatter;
import org.bukkit.entity.Villager;

public class VillagerTradeQuest extends Quest {


    // ---------------------------------------------------------------------------------------
    // Quest State
    // ---------------------------------------------------------------------------------------

    private final Villager.Profession profession;


    // ---------------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------------

    public VillagerTradeQuest(Villager.Profession profession, int goal, Reward reward) {
        super(goal, reward);
        this.profession = profession;
    }


    // ---------------------------------------------------------------------------------------
    // Functionality
    // ---------------------------------------------------------------------------------------

    @Override
    public QuestData toData() {
        QuestData data = super.toData();
        data.setQuestType(QuestType.VILLAGER_TRADE.name());
        data.setMaterial(profession.name());
        return data;
    }


    // ---------------------------------------------------------------------------------------
    // Getter & Setter
    // ---------------------------------------------------------------------------------------

    /**
     * @return String in the format: "Earn <amount> Emeralds from a <villager type>"
     */
    @Override
    public String getName() {
        String villagerTitle = (profession == Villager.Profession.NONE) ? "Villager" : StringFormatter.format(profession.toString());
        return String.format("Earn %s Emeralds from a %s", getGoal(), villagerTitle);
    }

    @Override
    public String[] getDecisionObjectNames() {
        return new String[]{QuestType.VILLAGER_TRADE.name(), profession.name()};
    }

    public Villager.Profession getProfession() {
        return profession;
    }

    @Override
    public QuestType getQuestType() {
        return QuestType.VILLAGER_TRADE;
    }

    @Override
    public String getOptionName() {
        return StringFormatter.format(profession.toString());
    }
}
