package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.jetbrains.annotations.NotNull;


public class BreakBlockListener implements Listener {

	/**
	 * Checks if the player has an active BlockBreakQuest with the according block. If so, updates the quests progress.
	 */
	@EventHandler
	public void onBreakBlock(@NotNull BlockBreakEvent event) {
		Block block = event.getBlock();

		QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer(event.getPlayer());
		if (questPlayer == null) return;

		for (Quest quest: questPlayer.getQuests()) {
			if (quest instanceof BlockBreakQuest) {
				handleBlockBreakQuest(questPlayer, block, (BlockBreakQuest) quest);
			} else if (quest instanceof ChopWoodQuest) {
				handleChopWoodQuest(questPlayer, block, (ChopWoodQuest) quest);
			} else if (quest instanceof MineBlockQuest) {
				handleMineBlockQuest(questPlayer, block, (MineBlockQuest) quest);
			}
		}
	}

	private void handleBlockBreakQuest(QuestPlayer questPlayer, Block block, BlockBreakQuest quest) {
		// Check whether the block has been placed by a player to prevent exploitation
		if (block.hasMetadata("basicquests.placed")) return;

		if (quest.getMaterial() == block.getType()) {
			quest.progress(1, questPlayer);
		}
	}

	private void handleChopWoodQuest(QuestPlayer questPlayer, Block block, ChopWoodQuest quest) {
		// Check whether the block has been placed by a player to prevent exploitation
		if (block.hasMetadata("basicquests.placed")) return;

		if (quest.getMaterial() == block.getType() ||
				(quest.getMaterialString() != null && quest.getMaterialString().equals("LOG") &&
						(block.getType() == Material.ACACIA_LOG ||
								block.getType() == Material.BIRCH_LOG ||
								block.getType() == Material.DARK_OAK_LOG ||
								block.getType() == Material.JUNGLE_LOG ||
								block.getType() == Material.OAK_LOG ||
								block.getType() == Material.SPRUCE_LOG))) {

			quest.progress(1, questPlayer);
		}
	}

	private void handleMineBlockQuest(QuestPlayer questPlayer, Block block, MineBlockQuest quest) {
		// Check whether the block has been placed by a player to prevent exploitation
		if (block.hasMetadata("basicquests.placed")) return;

		if (quest.getMaterial() == block.getType()) {
			quest.progress(1, questPlayer);
		}
	}
}
