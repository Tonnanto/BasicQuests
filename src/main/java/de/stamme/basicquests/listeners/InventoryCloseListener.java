package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        dropRemainingReward(event);
    }

    /** Drops the remaining content of the reward inventory if it is closed */
    private void dropRemainingReward(InventoryCloseEvent event) {

        QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(event.getPlayer().getUniqueId());
        if (questPlayer == null) {
            return;
        }

        if (questPlayer.getRewardInventory() != null) {
            ItemStack[] remainingRewards = questPlayer.getRewardInventory().getContents();
            for (ItemStack itemStack : remainingRewards) {
                if (itemStack == null) {
                    continue;
                }
                questPlayer.getPlayer().getWorld().dropItem(questPlayer.getPlayer().getLocation().add(0, 1, 0), itemStack);
            }
            questPlayer.getRewardInventory().clear();
        }

        questPlayer.setRewardInventory(null);
    }
}
