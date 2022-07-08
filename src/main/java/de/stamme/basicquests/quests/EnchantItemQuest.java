package de.stamme.basicquests.quests;

import de.stamme.basicquests.util.StringFormatter;
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

        String mat_name = StringFormatter.format(material.toString());

        // no enchantment requirement
        if (enchantment == null) {
            return String.format("Enchant %s %s%s", (getGoal() == 1) ? "a" : getGoal(), mat_name, (getGoal() > 1) ? "s" : "");

            // with enchantment requirement
        } else {
            String lvlString = StringFormatter.enchantmentLevel(enchantment, lvl);
            return String.format("Enchant %s %s%s with %s %s", (getGoal() == 1) ? "a" : getGoal(), mat_name, (getGoal() > 1) ? "s" : "", StringFormatter.enchantmentName(enchantment), (lvlString.length() > 0) ? lvlString + "+" : "");
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
}
