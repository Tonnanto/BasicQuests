package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

public class InventoryDragListener implements Listener {

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (Main.plugin.questPlayer.containsKey(event.getWhoClicked().getUniqueId())) {
                QuestPlayer player = Main.plugin.questPlayer.get(event.getWhoClicked().getUniqueId());

                if (player.rewardInventory != null && event.getInventory() == player.rewardInventory) {
                    for (int i: event.getInventorySlots()) {
                        if (i < player.rewardInventory.getSize()) {
                            event.setCancelled(true);
                            event.setResult(Event.Result.DENY);
                            return;
                        }
                    }
                }
            }
        }
    }
}
