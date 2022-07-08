package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.EnchantItemQuest;
import de.stamme.basicquests.quests.Quest;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InventoryClickListener implements Listener {


    @EventHandler
    public void onMoveItem(@NotNull InventoryClickEvent event) {

        if (event.getWhoClicked() instanceof Player) {
            if (Main.plugin.questPlayer.containsKey(event.getWhoClicked().getUniqueId())) {
                QuestPlayer questPlayer = Main.plugin.questPlayer.get(event.getWhoClicked().getUniqueId());

                cancelRewardInventoryPlace(questPlayer, event);

                listenForAnvilEnchantments(questPlayer, event);
            }
        }
    }

    /**
     * Prevents Player from moving items in the Reward Inventory
     */
    private void cancelRewardInventoryPlace(QuestPlayer questPlayer, @NotNull InventoryClickEvent event) {
        if (questPlayer.getRewardInventory() != null && event.getInventory() == questPlayer.getRewardInventory()) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (event.getClickedInventory() != questPlayer.getRewardInventory())
                    event.setCancelled(true);
            } else if (
                    event.getAction() == InventoryAction.PLACE_ALL ||
                            event.getAction() == InventoryAction.PLACE_ONE ||
                            event.getAction() == InventoryAction.PLACE_SOME ||
                            event.getAction() == InventoryAction.SWAP_WITH_CURSOR
            ) {
                if (event.getClickedInventory() == questPlayer.getRewardInventory())
                    event.setCancelled(true);
            }
        }
    }


    /**
     * Listens for Enchantments in Anvil
     */
    private void listenForAnvilEnchantments(QuestPlayer questPlayer, @NotNull InventoryClickEvent event) {
        for (Quest quest: questPlayer.getQuests()) {

            if (!(quest instanceof EnchantItemQuest)) continue;

            EnchantItemQuest eiq = (EnchantItemQuest) quest;
            Inventory inv = event.getInventory();

            if (!(inv instanceof AnvilInventory)) continue;
            // Is Anvil Inventory

            AnvilInventory anvil = (AnvilInventory) inv;
            InventoryView view = event.getView();
            int rawSlot = event.getRawSlot();

            if (rawSlot != view.convertSlot(rawSlot)) continue;
            // Is upper inventory

            if (rawSlot != 2) continue;
            // Is result slot

            ItemStack[] items = anvil.getContents();

            // item in result slot
            ItemStack resultItem = event.getCurrentItem();

            if (resultItem == null || eiq.getMaterial() != resultItem.getType()) continue;
            // Is correct result material

            Map<Enchantment, Integer> enchantments = resultItem.getEnchantments();

            if (enchantments.isEmpty()) continue;
            // Result has enchantments

            // item in the right slot
            // Should be an enchanted book
            ItemStack enchantedBookItem = items[1];

            if (enchantedBookItem == null || enchantedBookItem.getType() != Material.ENCHANTED_BOOK) continue;
            // Enchanted Book used

            if (eiq.getEnchantment() == null) {
                // No specific enchantment required
                eiq.progress(resultItem.getAmount(), questPlayer);

            } else if (enchantments.containsKey(eiq.getEnchantment()) && enchantments.get(eiq.getEnchantment()) >= eiq.getLvl()) {
                // Correct Enchantment
                eiq.progress(resultItem.getAmount(), questPlayer);
            }

            // Item has been enchanted in anvil !
        }
    }
}
