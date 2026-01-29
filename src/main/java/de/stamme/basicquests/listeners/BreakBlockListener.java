package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.BlockBreakQuest;
import de.stamme.basicquests.model.quests.ChopWoodQuest;
import de.stamme.basicquests.model.quests.MineBlockQuest;
import de.stamme.basicquests.model.quests.PickFlowerQuest;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.wrapper.material.QuestMaterialService;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class BreakBlockListener implements Listener {

    /**
     * Checks if the player has an active BlockBreakQuest with the according block. If so, updates
     * the quests progress.
     */
    @EventHandler
    public void onBreakBlock(@NotNull BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();

        QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(event.getPlayer());
        if (questPlayer == null) {
            return;
        }

        for (Quest quest : questPlayer.getQuests()) {
            if (quest instanceof BlockBreakQuest) {
                handleBlockBreakQuest(questPlayer, block, (BlockBreakQuest) quest);
            } else if (quest instanceof ChopWoodQuest) {
                handleChopWoodQuest(questPlayer, block, (ChopWoodQuest) quest);
            } else if (quest instanceof MineBlockQuest) {
                handleMineBlockQuest(questPlayer, block, (MineBlockQuest) quest);
            } else if (quest instanceof PickFlowerQuest) {
                handlePickFlowerQuest(questPlayer, block, (PickFlowerQuest) quest);
            }
        }
    }

    private void handleBlockBreakQuest(QuestPlayer questPlayer, Block block, BlockBreakQuest quest) {
        // Check whether the block has been placed by a player to prevent exploitation
        if (block.hasMetadata("basicquests.placed")) {
            return;
        }

        if (quest.getMaterial() == block.getType()) {
            quest.progress(1, questPlayer);
        }
    }

    private void handleChopWoodQuest(QuestPlayer questPlayer, Block block, ChopWoodQuest quest) {
        // Check whether the block has been placed by a player to prevent exploitation
        if (block.hasMetadata("basicquests.placed")) {
            return;
        }

        if (quest.getMaterial() == block.getType() || (quest.getMaterialString() != null && quest.getMaterialString().equals("LOG")
                && QuestMaterialService.getInstance().isLogType(block.getType()))) {
            quest.progress(1, questPlayer);
        }
    }

    private void handleMineBlockQuest(QuestPlayer questPlayer, Block block, MineBlockQuest quest) {
        // Check whether the block has been placed by a player to prevent exploitation
        if (block.hasMetadata("basicquests.placed")) {
            return;
        }

        boolean isCorrectMaterial = QuestMaterialService.getInstance().isCorrectMaterialForQuest(quest.getMaterial(), block.getType());

        if (isCorrectMaterial) {
            quest.progress(1, questPlayer);
        }
    }

    private void handlePickFlowerQuest(QuestPlayer questPlayer, Block block, PickFlowerQuest quest) {
        // Check whether the flower has been placed by a player to prevent exploitation
        if (block.hasMetadata("basicquests.placed")) {
            return;
        }

        // Check whether the lower flower block has been placed by player to prevent exploitation (Some flowers have a height of 2)
        Block blockBelow = block.getRelative(BlockFace.DOWN);
        if (blockBelow.getType() == block.getType() && blockBelow.hasMetadata("basicquests.placed")) {
            return;
        }

        if (quest.getMaterial() == block.getType()) {
            quest.progress(1, questPlayer);
        }
    }
}
