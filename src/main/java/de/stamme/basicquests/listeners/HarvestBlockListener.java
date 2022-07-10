package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.EnchantItemQuest;
import de.stamme.basicquests.quests.HarvestBlockQuest;
import de.stamme.basicquests.quests.Quest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class HarvestBlockListener implements Listener {

	@EventHandler
	public void onHarvestBlock(@NotNull PlayerHarvestBlockEvent event) {

		QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer(event.getPlayer());
		if (questPlayer == null) return;

		for (Quest quest: questPlayer.getQuests()) {
			if (quest instanceof HarvestBlockQuest) {
				handleHarvestBlockQuest(questPlayer, event, (HarvestBlockQuest) quest);
			}
		}
	}

	private void handleHarvestBlockQuest(QuestPlayer questPlayer, PlayerHarvestBlockEvent event, HarvestBlockQuest quest) {
		List<ItemStack> harvestedItems = event.getItemsHarvested();

		int yield = 0;
		for (ItemStack itemStack: harvestedItems) {
			if (itemStack.getType() == quest.getMaterial()) { yield += itemStack.getAmount(); }
		}

		if (yield > 0) { quest.progress(yield, questPlayer); }
	}
}
