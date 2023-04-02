package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.config.MinecraftLocaleConfig;
import de.stamme.basicquests.model.rewards.Reward;
import de.stamme.basicquests.util.StringFormatter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.text.MessageFormat;

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
        boolean withEnchantment = getEnchantment() != null;
        boolean withLevel = getLvl() > 0;
        int goal = getGoal();
        if (goal <= 1) {
            // Enchant 1 item
            String singularName = MinecraftLocaleConfig.getMinecraftName(getOptionKey(), "item.minecraft.");
            if (!withEnchantment) {
                return MessageFormat.format(MessagesConfig.getMessage("quests.enchant-item.any.singular"), singularName);
            }
            String enchantmentName = StringFormatter.enchantmentName(getEnchantment());
            if (!withLevel) {
                return MessageFormat.format(MessagesConfig.getMessage("quests.enchant-item.singular.withoutLevel"), singularName, enchantmentName);
            }
            String enchantmentLevel = StringFormatter.enchantmentLevel(getLvl(), getEnchantment());
            if (enchantmentLevel.length() <= 0) {
                return MessageFormat.format(MessagesConfig.getMessage("quests.enchant-item.singular.withoutLevel"), singularName, enchantmentName);
            }
            return MessageFormat.format(MessagesConfig.getMessage("quests.enchant-item.singular.generic"), singularName, enchantmentName, enchantmentLevel);

        } else {
            // Enchant multiple items (Books)
            String pluralName = MessagesConfig.getPluralName(getQuestType(), getOptionKey(), "item.minecraft.");
            if (!withEnchantment) {
                return MessageFormat.format(MessagesConfig.getMessage("quests.enchant-item.any.plural"), goal, pluralName);
            }
            String enchantmentName = StringFormatter.enchantmentName(getEnchantment());
            if (!withLevel) {
                return MessageFormat.format(MessagesConfig.getMessage("quests.enchant-item.plural.withoutLevel"), goal, pluralName, enchantmentName);
            }
            String enchantmentLevel = StringFormatter.enchantmentLevel(getLvl(), getEnchantment());
            if (enchantmentLevel.length() <= 0) {
                return MessageFormat.format(MessagesConfig.getMessage("quests.enchant-item.plural.withoutLevel"), goal, pluralName, enchantmentName);
            }
            return MessageFormat.format(MessagesConfig.getMessage("quests.enchant-item.plural.generic"), goal, pluralName, enchantmentName, enchantmentLevel);
        }
    }

    @Override
    public String[] getOptionNames() {
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
    public String getOptionKey() {
        return material.toString();
    }
}
