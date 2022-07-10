package de.stamme.basicquests.quests;

import de.stamme.basicquests.util.StringFormatter;
import org.bukkit.Material;

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
        if (materialString != null && !materialString.isEmpty())
            return String.format("Chop %s %ss", getGoal(), StringFormatter.format(materialString));
        else
            return String.format("Chop %s %ss", getGoal(), StringFormatter.format(material.toString()));
    }

    @Override
    public String[] getDecisionObjectNames() {
        return new String[]{QuestType.CHOP_WOOD.name(), getMaterial().name(), getMaterialString()};
    }

    public Material getMaterial() {
        return material;
    }

    public String getMaterialString() {
        return materialString;
    }
}
