package de.stamme.basicquests.quests;

import de.stamme.basicquests.util.StringFormatter;
import org.bukkit.Material;

public class ChopWoodQuest extends Quest {

    public Material material;
    public String materialString = ""; // Only used for "LOG"

    public ChopWoodQuest(Material mat, int goal, Reward reward) {
        super(goal, reward);
        this.material = mat;
    }

    // Initializer for 'Log' Quests that accept any kind of log
    public ChopWoodQuest(String mat, int goal, Reward reward) {
        super(goal, reward);
        this.material = Material.OAK_LOG; //
        this.materialString = mat;
    }

    public QuestData toData() {
        QuestData data = super.toData();

        data.questType = QuestType.CHOP_WOOD.name();
        data.material = material.name();
        data.materialString = materialString;

        return data;
    }

    // Returns a String in the format: "Chop <amount> <wood>"
    @Override
    public String getName() {
        if (materialString != null && !materialString.isEmpty())
            return String.format("Chop %s %ss", goal, StringFormatter.format(materialString));
        else
            return String.format("Chop %s %ss", goal, StringFormatter.format(material.toString()));
    }

    @Override
    public String[] getDecisionObjectNames() {
        return new String[]{QuestType.CHOP_WOOD.name(), material.name(), materialString};
    }
}
