package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

public class InventoryDragListener implements Listener {

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer((Player) event.getWhoClicked());
        if (questPlayer == null) return;

        cancelRewardInventoryPlace(questPlayer, event);
    }

    /**
     * Prevents Player from moving items in the Reward Inventory
     */
    private void cancelRewardInventoryPlace(QuestPlayer questPlayer, @NotNull InventoryDragEvent event) {
        if (questPlayer.getRewardInventory() == null || event.getInventory() != questPlayer.getRewardInventory()) return;

        for (int i: event.getInventorySlots()) {
            if (i < questPlayer.getRewardInventory().getSize()) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
                return;
            }
        }
    }
}
