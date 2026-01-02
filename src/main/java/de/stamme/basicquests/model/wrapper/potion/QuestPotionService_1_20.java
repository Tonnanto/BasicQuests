package de.stamme.basicquests.model.wrapper.potion;

import de.stamme.basicquests.BasicQuestsPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

import static org.bukkit.potion.PotionType.*;

// 1.20 changed the potion API!
// Extended and upgraded potion variants are now their own PotionTypes rather than a flag in the item's metadata
public class QuestPotionService_1_20 extends QuestPotionService {
    @Override
    @Nullable
    public ItemStack getPotionItemStack(Material material, String potionName, boolean extended, boolean upgraded, int amount) {

        PotionType basePotionType;
        try {
            basePotionType = PotionType.valueOf(potionName);
        } catch (Exception e) {
            BasicQuestsPlugin.log(Level.INFO, String.format("PotionType '%s' does not exist in this version.", potionName));
            return null;
        }

        PotionType potionType = getPotionType(basePotionType, extended, upgraded);

        ItemStack item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof PotionMeta)) {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not find PotionData for item with Material: " + item.getType().name());
            return null;
        }

        ((PotionMeta) itemMeta).setBasePotionType(potionType);
        item.setItemMeta(itemMeta);

        return item;
    }

    /**
     * Takes a base potion type and converts it into a new specific potion type based on whether its extended (LONG) or upgraded (STRONG)
     * These specific potion types were added in 1.20
     *
     * @param basePotionType the base potion type as found in potion_rewards.yml
     * @param extended       whether the potion type should be extended (LONG)
     * @param upgraded       whether the potion type should be upgraded (STRONG)
     * @return the new potion type
     */
    PotionType getPotionType(PotionType basePotionType, boolean extended, boolean upgraded) {
        if (!extended && !upgraded) {
            return basePotionType;
        }
        switch (basePotionType) {
            case NIGHT_VISION:
                if (extended)
                    return LONG_NIGHT_VISION;
                return NIGHT_VISION;
            case INVISIBILITY:
                if (extended)
                    return LONG_INVISIBILITY;
                return INVISIBILITY;
            case LEAPING:
                if (extended)
                    return LONG_LEAPING;
                return STRONG_LEAPING;
            case FIRE_RESISTANCE:
                if (extended)
                    return LONG_FIRE_RESISTANCE;
                return FIRE_RESISTANCE;
            case SWIFTNESS:
                if (extended)
                    return LONG_SWIFTNESS;
                return STRONG_SWIFTNESS;
            case SLOWNESS:
                if (extended)
                    return LONG_SLOWNESS;
                return STRONG_SLOWNESS;
            case WATER_BREATHING:
                if (extended)
                    return LONG_WATER_BREATHING;
                return WATER_BREATHING;
            case HEALING:
                if (extended)
                    return HEALING;
                return STRONG_HEALING;
            case HARMING:
                if (extended)
                    return HARMING;
                return STRONG_HARMING;
            case POISON:
                if (extended)
                    return LONG_POISON;
                return STRONG_POISON;
            case REGENERATION:
                if (extended)
                    return LONG_REGENERATION;
                return STRONG_REGENERATION;
            case STRENGTH:
                if (extended)
                    return LONG_STRENGTH;
                return STRONG_STRENGTH;
            case WEAKNESS:
                if (extended)
                    return LONG_WEAKNESS;
                return WEAKNESS;
            case TURTLE_MASTER:
                if (extended)
                    return LONG_TURTLE_MASTER;
                return STRONG_TURTLE_MASTER;
            case SLOW_FALLING:
                if (extended)
                    return LONG_SLOW_FALLING;
                return SLOW_FALLING;
            default:
                return basePotionType;
        }
    }
}
