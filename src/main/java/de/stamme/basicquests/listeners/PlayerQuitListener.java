package de.stamme.basicquests.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.PlayerData;
import de.stamme.basicquests.main.QuestPlayer;

public class PlayerQuitListener implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		
		if (Main.plugin.questPlayer.containsKey(event.getPlayer().getUniqueId())) {
			QuestPlayer player = Main.plugin.questPlayer.get(event.getPlayer().getUniqueId());
			
			PlayerData.getPlayerDataAndSave(player);
			Main.plugin.questPlayer.remove(player.player.getUniqueId());
		}
	}
}
