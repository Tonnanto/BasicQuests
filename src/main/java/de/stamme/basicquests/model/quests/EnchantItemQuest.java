package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.util.StringFormatter;
import java.text.MessageFormat;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public class EnchantItemQuest extends Quest {


    // ---------------------------------------------------------------------------------------
    // Quest State
    // ---------------------------------------------------------------------------------------

    private final Material material;
    private final Enchantment enchantment;
    private final int lvl;


    // ---------------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------------

    /**
     * If lvl does not matter -> lvl = 0
     */
    public EnchantItemQuest(Material mat, Enchantment enchantment, int lvl, int goal, Reward reward) {
        super(goal, reward);
        this.material = mat;
        this.enchantment = enchantment;
        this.lvl = lvl;
    }

    /**
     * Constructor without enchantment requirement
     */
    public EnchantItemQuest(Material mat, int goal, Reward reward) {
        super(goal, reward);
        this.material = mat;
        this.enchantment = null;
        this.lvl = 0;
    }


    // ---------------------------------------------------------------------------------------
    // Functionality
    // ---------------------------------------------------------------------------------------

	@Override
    public QuestData toData() {
        QuestData data = super.toData();
        data.setQuestType(QuestType.ENCHANT_ITEM.name());
        data.setMaterial(material.name());

        if (enchantment != null) {
            data.setEnchantment(enchantment.getKey().toString().split(":")[1]);
            data.setEnchantmentLvl(lvl);
        }

        return data;
    }


    // ---------------------------------------------------------------------------------------
    // Getter & Setter
    // ---------------------------------------------------------------------------------------

    /**
     * @return String in the format: "Enchant <amount> <material> with <enchantment> <lvl>"
     */
    @Override
    public String getName() {
        String materialName = StringFormatter.getLocalizedName(this.material.name(), "item.minecraft.");
        int goal = this.getGoal();
        if (this.enchantment == null) {
            // no enchantment requirement
            return goal > 1
                ? MessageFormat.format(Main.l10n("quest.enchantItem.any.plural"), goal, materialName)
                : MessageFormat.format(Main.l10n("quest.enchantItem.any.singular"), materialName);
        } else {
            // with enchantment requirement
            String enchantmentName = StringFormatter.enchantmentName(this.enchantment);
            String enchantmentLevel = StringFormatter.enchantmentLevel(this.lvl, this.enchantment);
            boolean hasLevel = enchantmentLevel.length() > 0;
            if (goal > 1) {
                return hasLevel
                    ? MessageFormat.format(Main.l10n("quest.enchantItem.plural"), goal, materialName, enchantmentName, enchantmentLevel)
                    : MessageFormat.format(Main.l10n("quest.enchantItem.plural.withoutLevel"), goal, materialName, enchantmentName);
            } else {
                return hasLevel
                    ? MessageFormat.format(Main.l10n("quest.enchantItem.singular"), materialName, enchantmentName, enchantmentLevel)
                    : MessageFormat.format(Main.l10n("quest.enchantItem.singular.withoutLevel"), materialName, enchantmentName);
            }
        }
    }

    @Override
    public String[] getDecisionObjectNames() {
        String enchantmentStr = (enchantment != null) ? enchantment.toString() : "";
        return new String[]{QuestType.ENCHANT_ITEM.name(), material.name(), enchantmentStr};
    }

    public Material getMaterial() {
        return material;
    }

	public Enchantment getEnchantment() {
		return enchantment;
	}

	public int getLvl() {
		return lvl;
	}

    @Override
    public final QuestType getQuestType() {
        return QuestType.ENCHANT_ITEM;
    }

    @Override
    public String getOptionName() {
        return StringFormatter.format(material.toString());
    }
}
