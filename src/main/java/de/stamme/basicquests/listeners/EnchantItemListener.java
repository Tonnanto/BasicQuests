package de.stamme.basicquests.listeners;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.EnchantItemQuest;
import de.stamme.basicquests.model.quests.Quest;
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
		QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer(event.getEnchanter());
		if (questPlayer == null) return;

		for (Quest quest : questPlayer.getQuests()) {
			if ((quest instanceof EnchantItemQuest)) {
				handleEnchantItemQuest(questPlayer, event, (EnchantItemQuest) quest);
			}

		}
	}

	private void handleEnchantItemQuest(QuestPlayer questPlayer, EnchantItemEvent event, EnchantItemQuest quest) {
		ItemStack item = event.getItem();
		Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();

		if (quest.getMaterial() != item.getType()) return;
		// correct material

		if (quest.getEnchantment() == null) {
			// no specific enchantment required
			quest.progress(1, questPlayer);

		} else if (enchantments.containsKey(quest.getEnchantment())) {
			// Correct Enchantment
			if (enchantments.get(quest.getEnchantment()) >= quest.getLvl()) {
				quest.progress(1, questPlayer);
			}
		}
	}
}
