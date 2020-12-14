package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.HarvestBlockQuest;
import de.stamme.basicquests.quests.Quest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HarvestBlockListener implements Listener {

	@EventHandler
	public void onHarvestBlock(PlayerHarvestBlockEvent event) {
		List<ItemStack> harvestedItems = event.getItemsHarvested();
		
		if (Main.plugin.questPlayer.containsKey(event.getPlayer().getUniqueId())) {
			QuestPlayer player = Main.plugin.questPlayer.get(event.getPlayer().getUniqueId());
			
			for (Quest q: player.quests) {
				if (q instanceof HarvestBlockQuest) {
					
					HarvestBlockQuest hbq = (HarvestBlockQuest) q;

					int yield = 0;
					for (ItemStack itemStack: harvestedItems) {
						if (itemStack.getType() == hbq.material) { yield += itemStack.getAmount(); }
					}

					if (yield > 0) { hbq.progress(yield, player); }
				}
			}
		}
	}
}
