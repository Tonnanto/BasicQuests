package de.stamme.basicquests.listeners;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.QuestPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (Main.getPlugin().getQuestPlayers().containsKey(event.getPlayer().getUniqueId())) {
            QuestPlayer questPlayer = Main.getPlugin().getQuestPlayers().get(event.getPlayer().getUniqueId());
            questPlayer.setRewardInventory(null);
        }
    }
}
