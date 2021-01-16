package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class InventoryClickListener implements Listener {

    // Prevent Player from moving items in the Reward Inventory
    @EventHandler
    public void onMoveItem(@NotNull InventoryClickEvent event) {

        if (event.getWhoClicked() instanceof Player) {
            if (Main.plugin.questPlayer.containsKey(event.getWhoClicked().getUniqueId())) {
                QuestPlayer player = Main.plugin.questPlayer.get(event.getWhoClicked().getUniqueId());

                if (player.rewardInventory != null && event.getInventory() == player.rewardInventory) {
                    if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        if (event.getClickedInventory() != player.rewardInventory)
                            event.setCancelled(true);
                    } else if (
                            event.getAction() == InventoryAction.PLACE_ALL ||
                            event.getAction() == InventoryAction.PLACE_ONE ||
                            event.getAction() == InventoryAction.PLACE_SOME ||
                            event.getAction() == InventoryAction.SWAP_WITH_CURSOR
                    ) {
                        if (event.getClickedInventory() == player.rewardInventory)
                            event.setCancelled(true);
                    }
                }
            }
        }
    }
}
