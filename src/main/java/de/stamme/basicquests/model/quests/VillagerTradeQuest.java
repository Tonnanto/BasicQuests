package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.config.MinecraftLocaleConfig;
import de.stamme.basicquests.model.rewards.Reward;
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
        data.setMaterial(profession.getKey().getKey());
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
            ? MinecraftLocaleConfig.getMinecraftName("VILLAGER", "entity.minecraft.")
            : MinecraftLocaleConfig.getMinecraftName(profession.getKey().getKey(), "entity.minecraft.villager.");
        return goal > 1
            ? MessageFormat.format(MessagesConfig.getMessage("quests.villager-trade.plural"), villagerName, goal)
            : MessageFormat.format(MessagesConfig.getMessage("quests.villager-trade.singular"), villagerName);
    }

    @Override
    public String[] getOptionNames() {
        return new String[]{QuestType.VILLAGER_TRADE.name(), profession.getKey().getKey()};
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
