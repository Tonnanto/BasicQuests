package de.stamme.basicquests.model.wrapper.potion;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.wrapper.BukkitVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class QuestPotionService {

    private static QuestPotionService instance;

    /**
     * @return the QuestPotionService that handles Structures correctly for the current spigot version of the server.
     */
    public static QuestPotionService getInstance() {
        if (instance == null) {
            if (BasicQuestsPlugin.getBukkitVersion().isBelowOrEqual(BukkitVersion.v1_21)) {
                instance = new QuestPotionService_1_20();
            }
        }
        return instance;
    }

    @Nullable
    public abstract ItemStack getPotionItemStack(Material material, String potionName, boolean extended, boolean upgraded, int amount);
}
