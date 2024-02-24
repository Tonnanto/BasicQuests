package de.stamme.basicquests.model.wrapper.potion;

import de.stamme.basicquests.BasicQuestsPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

// Only compatible with spigot 1.16 - 1.19!
public class QuestPotionService_1_19 extends QuestPotionService {
    @Override
    @Nullable
    public ItemStack getPotionItemStack(Material material, String potionName, boolean extended, boolean upgraded, int amount) {

        PotionType potionType;
        try {
            potionType = PotionType.valueOf(potionName);
        } catch (Exception e) {
            BasicQuestsPlugin.log(Level.INFO,String.format("PotionType '%s' does not exist in this version.", potionName));
            return null;
        }


        ItemStack item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof PotionMeta)) {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not find PotionData for item with Material: " + item.getType().name());
            return null;
        }

        ((PotionMeta) itemMeta).setBasePotionData(new PotionData(potionType, extended, upgraded));
        item.setItemMeta(itemMeta);

        return item;
    }
}
