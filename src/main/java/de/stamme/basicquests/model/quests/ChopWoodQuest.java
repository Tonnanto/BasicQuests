package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.wrapper.material.QuestMaterialService;
import de.stamme.basicquests.util.StringFormatter;
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

        if (materialString != null && !materialString.isEmpty())
            return MessageFormat.format(Main.l10n("quests.title.chopWood"), this.getGoal(), Main.l10n("material.logs"));
        else
            return MessageFormat.format(Main.l10n("quests.title.chopWood"), this.getGoal(), StringFormatter.localizedMaterial(material));
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
