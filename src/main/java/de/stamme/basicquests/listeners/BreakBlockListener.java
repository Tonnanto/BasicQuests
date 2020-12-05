package de.stamme.basicquests.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.BlockBreakQuest;
import de.stamme.basicquests.quests.MineBlockQuest;
import de.stamme.basicquests.quests.Quest;


public class BreakBlockListener implements Listener {

	// Checks if the player has an active BlockBreakQuest with the according block. If so, updates the quests progress.
	@EventHandler
	public void onBreakBlock(BlockBreakEvent event) {
		Block block = event.getBlock();
		
		if (Main.plugin.questPlayer.containsKey(event.getPlayer().getUniqueId())) {
			QuestPlayer player = Main.plugin.questPlayer.get(event.getPlayer().getUniqueId());
			
			// Check whether the block has been placed by a player to prevent exploitation
			boolean placedByPlayer = block.hasMetadata("basicquests.placed");
//			if (placedByPlayer) {
//				block.removeMetadata("basicquests.placed", Main.plugin);
//			}
			
			for (Quest q: player.quests) {
				// BlockBreakQuest
				if (q instanceof BlockBreakQuest && !placedByPlayer) {
					BlockBreakQuest bbq = (BlockBreakQuest) q;
					
					
					if (bbq.material == block.getType() |
							(bbq.materialString != null && bbq.materialString.equals("LOG") && 
								(block.getType() == Material.ACACIA_LOG |
								block.getType() == Material.BIRCH_LOG |
								block.getType() == Material.DARK_OAK_LOG |
								block.getType() == Material.JUNGLE_LOG |
								block.getType() == Material.OAK_LOG |
								block.getType() == Material.SPRUCE_LOG))) {
						
						bbq.progress(1, player);
					}
				}
				
				// MineBlockQuest
				else if (q instanceof MineBlockQuest && !placedByPlayer) {
					MineBlockQuest mbq = (MineBlockQuest) q;
					if (mbq.material == block.getType()) {
						
						mbq.progress(1, player);
					}
				}
			}
		}
	}
}
