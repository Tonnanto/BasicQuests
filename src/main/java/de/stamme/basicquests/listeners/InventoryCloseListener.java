package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (Main.plugin.questPlayer.containsKey(event.getPlayer().getUniqueId())) {
            QuestPlayer questPlayer = Main.plugin.questPlayer.get(event.getPlayer().getUniqueId());
            questPlayer.setRewardInventory(null);
        }
    }
}
