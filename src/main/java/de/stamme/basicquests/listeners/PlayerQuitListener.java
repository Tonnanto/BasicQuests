package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.PlayerData;
import de.stamme.basicquests.main.QuestPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerQuitListener implements Listener {

	@EventHandler
	public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
		
		if (Main.plugin.questPlayer.containsKey(event.getPlayer().getUniqueId())) {
			QuestPlayer player = Main.plugin.questPlayer.get(event.getPlayer().getUniqueId());
			
			if (PlayerData.getPlayerDataAndSave(player))
				Main.log("PlayerData Saved: " + player.getName());
			else
				Main.log("Failed to save PlayerData: " + player.getName());
			Main.plugin.questPlayer.remove(player.player.getUniqueId());
		}
	}
}
