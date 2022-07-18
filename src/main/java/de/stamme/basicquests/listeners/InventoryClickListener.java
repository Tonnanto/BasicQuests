package de.stamme.basicquests.listeners;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.EnchantItemQuest;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.quests.VillagerTradeQuest;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InventoryClickListener implements Listener {


    @EventHandler
    public void onMoveItem(@NotNull InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        if (Main.getPlugin().getQuestPlayers().containsKey(event.getWhoClicked().getUniqueId())) {
            QuestPlayer questPlayer = Main.getPlugin().getQuestPlayers().get(event.getWhoClicked().getUniqueId());

            listenForVillagerTrade(questPlayer, event);

            cancelRewardInventoryPlace(questPlayer, event);

            listenForAnvilEnchantments(questPlayer, event);
        }

    }

    /**
     * Prevents Player from moving items in the Reward Inventory
     */
    private void cancelRewardInventoryPlace(QuestPlayer questPlayer, @NotNull InventoryClickEvent event) {
        if (questPlayer.getRewardInventory() == null || event.getInventory() != questPlayer.getRewardInventory()) return;

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (event.getInventory() != questPlayer.getRewardInventory())
                event.setCancelled(true);
        } else if (
                event.getAction() == InventoryAction.PLACE_ALL ||
                        event.getAction() == InventoryAction.PLACE_ONE ||
                        event.getAction() == InventoryAction.PLACE_SOME ||
                        event.getAction() == InventoryAction.SWAP_WITH_CURSOR
        ) {
            if (event.getInventory() == questPlayer.getRewardInventory())
                event.setCancelled(true);
        }
    }


    /**
     * Listens for Villager trades
     */
    private void listenForVillagerTrade(QuestPlayer questPlayer, @NotNull InventoryClickEvent event) {
        for (Quest quest: questPlayer.getQuests()) {

            if (!(quest instanceof VillagerTradeQuest)) continue;
            VillagerTradeQuest vtq = (VillagerTradeQuest) quest;

            if (!(event.getInventory() instanceof MerchantInventory)) continue;
            MerchantInventory villagerInventory = (MerchantInventory) event.getInventory();
            // Is Merchant / Villager inventory

            if (!(villagerInventory.getHolder() instanceof Villager)) continue;
            Villager villager = (Villager) villagerInventory.getHolder();
            // Is Villager

            if (vtq.getProfession() != Villager.Profession.NONE && vtq.getProfession() != villager.getProfession()) continue;
            // Has correct profession

            InventoryView view = event.getView();
            int rawSlot = event.getRawSlot();

            if (rawSlot != view.convertSlot(rawSlot)) continue;
            // Is upper inventory

            if (rawSlot != 2) continue;
            // Is result slot

            // item in result slot
            ItemStack resultItem = event.getCurrentItem();
            if (resultItem != null && resultItem.getType() != Material.EMERALD) continue;
            // Emerald in result slot

            if (
                event.getAction() != InventoryAction.PICKUP_ALL &&
                event.getAction() != InventoryAction.PICKUP_HALF &&
                event.getAction() != InventoryAction.PICKUP_ONE &&
                event.getAction() != InventoryAction.PICKUP_SOME &&
                event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY &&
                event.getAction() != InventoryAction.HOTBAR_SWAP
            ) continue;
            // Picked up emeralds

            assert resultItem != null;
            vtq.progress(resultItem.getAmount(), questPlayer);
        }
    }


    /**
     * Listens for Enchantments in Anvil
     */
    private void listenForAnvilEnchantments(QuestPlayer questPlayer, @NotNull InventoryClickEvent event) {
        for (Quest quest: questPlayer.getQuests()) {

            if (!(quest instanceof EnchantItemQuest)) continue;
            EnchantItemQuest eiq = (EnchantItemQuest) quest;

            if (!(event.getInventory() instanceof AnvilInventory)) continue;
            // Is Anvil Inventory

            AnvilInventory anvil = (AnvilInventory) event.getInventory();
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

            if (
                event.getAction() != InventoryAction.PICKUP_ALL &&
                event.getAction() != InventoryAction.PICKUP_HALF &&
                event.getAction() != InventoryAction.PICKUP_ONE &&
                event.getAction() != InventoryAction.PICKUP_SOME &&
                event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY &&
                event.getAction() != InventoryAction.HOTBAR_SWAP
            ) continue;
            // Picked up result item

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
