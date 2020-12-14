package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.EnchantItemQuest;
import de.stamme.basicquests.quests.Quest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EnchantItemListener implements Listener {
	
	@EventHandler
	public void onEnchantItem(EnchantItemEvent event) {
		ItemStack item = event.getItem();
		Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();

		if (Main.plugin.questPlayer.containsKey(event.getEnchanter().getUniqueId())) {
			QuestPlayer player = Main.plugin.questPlayer.get(event.getEnchanter().getUniqueId());

			for (Quest q: player.quests) {
				
				if (q instanceof EnchantItemQuest) {
					EnchantItemQuest eiq = (EnchantItemQuest) q;
					
					if (eiq.material == item.getType()) {
						// correct material
						if (eiq.enchantment == null) {
							eiq.progress(1, player);
						
						} else if (enchantments.containsKey(eiq.enchantment)) {
							// Correct Enchantment
							if (enchantments.get(eiq.enchantment) >= eiq.lvl) {
								eiq.progress(1, player);
							}
						}
					}
				}
			}
		}
	}
}
