package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.PlayerData;
import de.stamme.basicquests.main.QuestPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class PlayerQuitListener implements Listener {

	@EventHandler
	public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
		
		if (Main.getPlugin().getQuestPlayers().containsKey(event.getPlayer().getUniqueId())) {
			QuestPlayer questPlayer = Main.getPlugin().getQuestPlayers().get(event.getPlayer().getUniqueId());
			
			if (PlayerData.getPlayerDataAndSave(questPlayer))
				Main.log("PlayerData Saved: " + questPlayer.getName());
			else
				Main.log(Level.SEVERE, "Failed to save PlayerData: " + questPlayer.getName());
			Main.getPlugin().getQuestPlayers().remove(questPlayer.getPlayer().getUniqueId());
		}
	}
}
