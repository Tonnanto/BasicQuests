package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.HarvestBlockQuest;
import de.stamme.basicquests.model.quests.Quest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlockDropItemListener implements Listener {

    @EventHandler
    public void onBlockDropItem(@NotNull BlockDropItemEvent event) {

        QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(event.getPlayer());
        if (questPlayer == null) return;

        for (Quest quest : questPlayer.getQuests()) {
            if (quest instanceof HarvestBlockQuest) {
                HarvestBlockQuest hbq = (HarvestBlockQuest) quest;
                handleHarvestBlockQuest(questPlayer, event, hbq);
            }
        }
    }

    private void handleHarvestBlockQuest(QuestPlayer questPlayer, BlockDropItemEvent event, HarvestBlockQuest quest) {
        Block block = event.getBlock();

        Material harvestedMaterial = event.getBlockState().getType();
        int yield = 0;

        // yield determined by height of blocks
        if (harvestedMaterial == Material.SUGAR_CANE ||
            harvestedMaterial == Material.BAMBOO ||
            harvestedMaterial == Material.CACTUS ||
            harvestedMaterial == Material.KELP_PLANT) {

            boolean isCorrectMaterial = quest.getMaterial() == harvestedMaterial || (quest.getMaterial() == Material.KELP && harvestedMaterial == Material.KELP_PLANT);
            if (!isCorrectMaterial) return;

            if (!event.getBlockState().hasMetadata("basicquests.placed")) {
                yield = 1;
            } // only raise yield if block has not been placed by player

            Block blockAbove = block.getRelative(BlockFace.UP);

            while (blockAbove.getType() == harvestedMaterial) {
                if (!blockAbove.hasMetadata("basicquests.placed")) {
                    yield++;
                } // only raise yield if block has not been placed by player
                blockAbove = blockAbove.getRelative(BlockFace.UP);
            }

            if (yield > 0) {
                quest.progress(yield, questPlayer);
            }
            return;
        }


        // yield determined by number of item drops
        for (Item item : event.getItems()) {

            if (item.getItemStack().getType() != quest.getMaterial()) continue;
            // Material from the quest has been dropped

            ItemStack itemStack = item.getItemStack();
            Material droppedBy = quest.getMaterial(); // default: Item drops itself (Wheat, ...)

            if (itemStack.getType() == Material.MELON_SLICE) {
                droppedBy = Material.MELON;
            } else if (itemStack.getType() == Material.POTATO) {
                droppedBy = Material.POTATOES;
            } else if (itemStack.getType() == Material.CARROT) {
                droppedBy = Material.CARROTS;
            } else if (itemStack.getType() == Material.BEETROOT) {
                droppedBy = Material.BEETROOTS;
            } else if (itemStack.getType() == Material.COCOA_BEANS) {
                droppedBy = Material.COCOA;
            }

            if (harvestedMaterial != droppedBy) break; // crop dropped from the wrong block (like a chest)

            if (event.getBlockState().getBlockData() instanceof Ageable) { // ageable Crops
                Ageable a = (Ageable) event.getBlockState().getBlockData();
                if (a.getAge() != a.getMaximumAge()) {
                    yield--;
                } // reduce yield by 1 if crop is not fully grown (prevent exploitation)

            } else { // not ageable crops (Pumpkin, Melon)
                if (event.getBlockState().hasMetadata("basicquests.placed")) {
                    yield--;
                } // reduce yield by 1 if crop placed by player (prevent exploitation)
            }

            yield += itemStack.getAmount();
        }


        if (yield > 0) {
            quest.progress(yield, questPlayer);
        }
    }
}
