package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.HarvestBlockQuest;
import de.stamme.basicquests.model.quests.Quest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HarvestBlockListener implements Listener {

	@EventHandler
	public void onHarvestBlock(@NotNull PlayerHarvestBlockEvent event) {
        if (event.isCancelled()) return;

		QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(event.getPlayer());
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
