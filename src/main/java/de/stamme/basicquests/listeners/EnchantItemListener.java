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
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EnchantItemListener implements Listener {
	
	@EventHandler
	public void onEnchantItem(@NotNull EnchantItemEvent event) {
		ItemStack item = event.getItem();
		Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();

		if (Main.getPlugin().getQuestPlayers().containsKey(event.getEnchanter().getUniqueId())) {
			QuestPlayer questPlayer = Main.getPlugin().getQuestPlayers().get(event.getEnchanter().getUniqueId());

			for (Quest quest: questPlayer.getQuests()) {
				
				if (quest instanceof EnchantItemQuest) {
					EnchantItemQuest eiq = (EnchantItemQuest) quest;
					
					if (eiq.getMaterial() == item.getType()) {
						// correct material
						if (eiq.getEnchantment() == null) {
							eiq.progress(1, questPlayer);
						
						} else if (enchantments.containsKey(eiq.getEnchantment())) {
							// Correct Enchantment
							if (enchantments.get(eiq.getEnchantment()) >= eiq.getLvl()) {
								eiq.progress(1, questPlayer);
							}
						}
					}
				}
			}
		}
	}
}
