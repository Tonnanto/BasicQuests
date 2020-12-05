package de.stamme.basicquests.listeners;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.HarvestBlockQuest;
import de.stamme.basicquests.quests.Quest;

public class BlockDropItemListener implements Listener {

	@EventHandler
	public void onBlockDropItem(BlockDropItemEvent event) {
		Block block = event.getBlock();
		
		if (Main.plugin.questPlayer.containsKey(event.getPlayer().getUniqueId())) {
			QuestPlayer player = Main.plugin.questPlayer.get(event.getPlayer().getUniqueId());
			
			for (Quest q: player.quests) {
				if (q instanceof HarvestBlockQuest) {
					HarvestBlockQuest hbq = (HarvestBlockQuest) q;
					
					Material harvestedMaterial = event.getBlockState().getType();
					int yield = 0;
					
					// yield determined by height of blocks
					if (harvestedMaterial == Material.SUGAR_CANE |
						harvestedMaterial == Material.BAMBOO |
						harvestedMaterial == Material.CACTUS |
						harvestedMaterial == Material.KELP_PLANT) {
						
						if (hbq.material == harvestedMaterial | (hbq.material == Material.KELP && harvestedMaterial == Material.KELP_PLANT)) {
							if (!event.getBlockState().hasMetadata("basicquests.placed")) { yield = 1; } // only raise yield if block has not been placed by player
							
							Block blockAbove = block.getRelative(BlockFace.UP);
							
							while(blockAbove != null && blockAbove.getType() == harvestedMaterial) {
								if (!blockAbove.hasMetadata("basicquests.placed")) { yield++; } // only raise yield if block has not been placed by player
								blockAbove = blockAbove.getRelative(BlockFace.UP);
							}
						}
					} 
					
					
					
					// yield determined by amount of item drops
					else {
						
						for (Item item: event.getItems()) {
							
//							player.sendMessage("block: " + harvestedMaterial.name() + "drop: " + item.getItemStack().getType().name());
							
							if (item.getItemStack().getType() == hbq.material) {
								ItemStack itemStack = item.getItemStack();
								
								Material droppedBy = hbq.material; // default: Item drops itself (Wheat, ...)
								
								if (itemStack.getType() == Material.MELON_SLICE) { droppedBy = Material.MELON; }
								else if (itemStack.getType() == Material.POTATO) { droppedBy = Material.POTATOES; }
								else if (itemStack.getType() == Material.CARROT) { droppedBy = Material.CARROTS; }
								else if (itemStack.getType() == Material.BEETROOT) { droppedBy = Material.BEETROOTS; }
								else if (itemStack.getType() == Material.COCOA_BEANS) { droppedBy = Material.COCOA; }								
								
								if (harvestedMaterial != droppedBy) { break; } // crop dropped from wrong block (p.e. chest)
								
								
								if (event.getBlockState().getBlockData() instanceof Ageable) { // ageable Crops
									Ageable a = (Ageable) event.getBlockState().getBlockData();
									if (a.getAge() != a.getMaximumAge()) { yield--; } // reduce yield by 1 if crop is not fully grown (prevent exploitation)
									
								} else { // not ageable crops (Pumpkin, Melon)
									if (event.getBlockState().hasMetadata("basicquests.placed")) { yield--; } // reduce yield by 1 if crop placed by player (prevent exploitation)
								}
								
								
								yield += itemStack.getAmount();
							}
						}
					}
					
					if (yield > 0) {
						hbq.progress(yield, player);
					}
				}
			}
		}
	}
	
}
