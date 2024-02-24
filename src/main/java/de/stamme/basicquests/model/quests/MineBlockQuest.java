package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.config.MinecraftLocaleConfig;
import de.stamme.basicquests.model.rewards.Reward;
import org.bukkit.Material;

import java.text.MessageFormat;

public class MineBlockQuest extends Quest {


    // ---------------------------------------------------------------------------------------
    // Quest State
    // ---------------------------------------------------------------------------------------

    private final Material material;


    // ---------------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------------

    public MineBlockQuest(Material mat, int goal, Reward reward) {
        super(goal, reward);
        this.material = mat;
    }


    // ---------------------------------------------------------------------------------------
    // Functionality
    // ---------------------------------------------------------------------------------------

    @Override
    public QuestData toData() {
        QuestData data = super.toData();
        data.setQuestType(QuestType.MINE_BLOCK.name());
        data.setMaterial(material.name());
        return data;
    }


    // ---------------------------------------------------------------------------------------
    // Getter & Setter
    // ---------------------------------------------------------------------------------------

    /**
     * @return String in the format: "Mine <amount> <material>"
     */
    @Override
    public String getName() {
        int goal = getGoal();
        if (goal <= 1) {
            String singularName = MinecraftLocaleConfig.getMinecraftName(getOptionKey(), "block.minecraft.");
            return MessageFormat.format(MessagesConfig.getMessage("quests.mine-block.singular"), singularName);
        } else {
            String pluralName = MessagesConfig.getPluralName(getQuestType(), getOptionKey(), "block.minecraft.");
            return MessageFormat.format(MessagesConfig.getMessage("quests.mine-block.plural"), goal, pluralName);
        }
    }

    @Override
    public String[] getOptionNames() {
        return new String[]{QuestType.MINE_BLOCK.name(), material.name()};
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public final QuestType getQuestType() {
        return QuestType.MINE_BLOCK;
    }

    @Override
    public String getOptionKey() {
        return material.toString();
    }
}
