package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.GainLevelQuest;
import de.stamme.basicquests.quests.HarvestBlockQuest;
import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.quests.ReachLevelQuest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerLevelChangeListener implements Listener {

	@EventHandler
	public void onPlayerLevelChange(@NotNull PlayerLevelChangeEvent event) {

		QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer(event.getPlayer());
		if (questPlayer == null) return;

		for (Quest quest : questPlayer.getQuests()) {
			// GainLevelQuest
			if (quest instanceof GainLevelQuest) {
				handleGainLevelQuest(questPlayer, event, (GainLevelQuest) quest);
			}

			// ReachLevelQuest
			if (quest instanceof ReachLevelQuest) {
				handleReachLevelQuest(questPlayer, event, (ReachLevelQuest) quest);
			}
		}
	}

	private void handleGainLevelQuest(QuestPlayer questPlayer, PlayerLevelChangeEvent event, GainLevelQuest quest) {
		int newLevel = event.getNewLevel();
		int oldLevel = event.getOldLevel();

		if (newLevel > oldLevel) {
			quest.progress(newLevel - oldLevel, questPlayer);
		}
	}

	private void handleReachLevelQuest(QuestPlayer questPlayer, PlayerLevelChangeEvent event, ReachLevelQuest quest) {
		int newLevel = event.getNewLevel();

		if (quest.isCompleted()) return;

		if (newLevel < quest.getGoal()) {
			quest.setCount(newLevel);
			quest.progress(0, questPlayer); // purpose: progress notification to player
		} else {
			quest.progress(quest.getGoal() - quest.getCount(), questPlayer);
		}
	}
}
