package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.GainLevelQuest;
import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.quests.ReachLevelQuest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerLevelChangeListener implements Listener {

	@EventHandler
	public void onPlayerLevelChange(@NotNull PlayerLevelChangeEvent event) {
		
		int newLevel = event.getNewLevel();
		int oldLevel = event.getOldLevel();
		
		if (Main.plugin.questPlayer.containsKey(event.getPlayer().getUniqueId())) {
			QuestPlayer questPlayer = Main.plugin.questPlayer.get(event.getPlayer().getUniqueId());
			
			for (Quest q: questPlayer.getQuests()) {
				
				// GainLevelQuest
				if (q instanceof GainLevelQuest) {
					GainLevelQuest glq = (GainLevelQuest) q;
					
					if (newLevel > oldLevel) {
						glq.progress(newLevel - oldLevel, questPlayer);
					}
				}
				
				
				// ReachLevelQuest
				if (q instanceof ReachLevelQuest) {
					ReachLevelQuest rlq = (ReachLevelQuest) q;
					
					if (!rlq.isCompleted()) {
						if (newLevel < rlq.getGoal()) {
							rlq.setCount(newLevel);
							rlq.progress(0, questPlayer); // purpose: progress notification to player
						} else {
							rlq.progress(rlq.getGoal() - rlq.getCount(), questPlayer);
						}
					}
				}
			}
		}
	}
	
}
