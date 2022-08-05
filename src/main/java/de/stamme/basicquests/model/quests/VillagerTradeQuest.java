package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.util.L10n;
import org.bukkit.entity.Villager;

import java.text.MessageFormat;

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
     * @return String in the format: "Trade with a <villager type> <amount> times"
     */
    @Override
    public String getName() {
        int goal = this.getGoal();
        String villagerName = (this.profession == Villager.Profession.NONE)
            ? L10n.getMinecraftName("VILLAGER", "entity.minecraft.")
            : L10n.getMinecraftName(profession.name(), "entity.minecraft.villager.");
        return goal > 1
            ? MessageFormat.format(L10n.getMessage("quest.villagerTrade.plural"), villagerName, goal)
            : MessageFormat.format(L10n.getMessage("quest.villagerTrade.singular"), villagerName);
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
    public String getOptionKey() {
        return (profession == Villager.Profession.NONE) ? "VILLAGER" : profession.toString();
    }
}
