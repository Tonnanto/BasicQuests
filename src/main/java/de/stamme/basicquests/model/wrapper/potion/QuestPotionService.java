package de.stamme.basicquests.model.wrapper.potion;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class QuestPotionService {

  private static QuestPotionService instance;

  /**
   * @return the QuestPotionService that handles Structures correctly for the current spigot version
   *     of the server.
   */
  public static QuestPotionService getInstance() {
    if (instance == null) {
      // This version only support v1.21+
      instance = new QuestPotionService_1_21();
    }
    return instance;
  }

  @Nullable
  public abstract ItemStack getPotionItemStack(
      Material material, String potionName, boolean extended, boolean upgraded, int amount);
}
