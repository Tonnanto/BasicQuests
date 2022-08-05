package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.util.StringFormatter;
import java.text.MessageFormat;
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
        int goal = this.getGoal();
        if (this.materialString == null || this.materialString.isEmpty()) {
            String materialName = StringFormatter.getLocalizedName(this.material.name(), "block.minecraft.");
            return goal > 1
                ? MessageFormat.format(Main.l10n("quest.chopWood.plural"), goal, materialName)
                : MessageFormat.format(Main.l10n("quest.chopWood.singular"), materialName);
        } else {
            return goal > 1 ? MessageFormat.format(Main.l10n("quest.chopWood.any.plural"), goal) : Main.l10n("quest.chopWood.any.singular");
        }
    }

    @Override
    public String[] getDecisionObjectNames() {
        return new String[]{QuestType.CHOP_WOOD.name(), getMaterial().name(), getMaterialString()};
    }

    @Override
    public String getOptionName() {
        if (materialString != null && !materialString.isEmpty())
            return StringFormatter.format(materialString);
        else
            return StringFormatter.format(material.toString());
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
