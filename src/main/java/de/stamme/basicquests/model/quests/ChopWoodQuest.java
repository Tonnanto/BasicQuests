package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.util.L10n;
import org.bukkit.Material;

import java.text.MessageFormat;

public class ChopWoodQuest extends Quest {


    // ---------------------------------------------------------------------------------------
    // Quest State
    // ---------------------------------------------------------------------------------------

    private final Material material;
    private final String materialString; // Only used for "LOG"


    // ---------------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------------

    public ChopWoodQuest(Material mat, int goal, Reward reward) {
        super(goal, reward);
        this.material = mat;
        this.materialString = "";
    }

    /**
     * Initializer for 'Log' Quests that accept any kind of log
     * @param mat == "LOG"
     */
    public ChopWoodQuest(String mat, int goal, Reward reward) {
        super(goal, reward);
        this.material = Material.OAK_LOG;
        this.materialString = mat;
    }


    // ---------------------------------------------------------------------------------------
    // Functionality
    // ---------------------------------------------------------------------------------------

    @Override
    public QuestData toData() {
        QuestData data = super.toData();
        data.setQuestType(QuestType.CHOP_WOOD.name());
        data.setMaterial(material.name());
        data.setMaterialString(materialString);
        return data;
    }


    // ---------------------------------------------------------------------------------------
    // Getter & Setter
    // ---------------------------------------------------------------------------------------

    /**
     * @return String in the format: "Chop <amount> <wood>"
     */
    @Override
    public String getName() {
        int goal = this.getGoal();

        if (getMaterialString() == null || getMaterialString().isEmpty()) {
            // Specific Log
            if (goal <= 1) {
                String singularName = L10n.getMinecraftName(getOptionKey(), "block.minecraft.");
                return MessageFormat.format(L10n.getMessage("quest.chopWood.singular"), singularName);
            } else {
                String pluralName = L10n.getLocalizedPluralName(getQuestType(), getOptionKey(), "block.minecraft.");
                return MessageFormat.format(L10n.getMessage("quest.chopWood.plural"), goal, pluralName);
            }

        } else {
            // Any Log
            if (goal <= 1) {
                return L10n.getMessage("quest.chopWood.any.singular");
            } else {
                return MessageFormat.format(L10n.getMessage("quest.chopWood.any.plural"), goal);
            }
        }
    }

    @Override
    public String[] getDecisionObjectNames() {
        return new String[]{QuestType.CHOP_WOOD.name(), getMaterial().name(), getMaterialString()};
    }

    @Override
    public String getOptionKey() {
        if (materialString != null && !materialString.isEmpty())
            return materialString;
        else
            return material.toString();
    }

    public Material getMaterial() {
        return material;
    }

    public String getMaterialString() {
        return materialString;
    }

    @Override
    public final QuestType getQuestType() {
        return QuestType.CHOP_WOOD;
    }
}
