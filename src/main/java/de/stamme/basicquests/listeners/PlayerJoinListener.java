package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.PlayerData;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quest_generation.QuestGenerationException;
import de.stamme.basicquests.quest_generation.QuestGenerator;
import de.stamme.basicquests.quests.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
		Player player = event.getPlayer();

		
		// load player data from file - if not successful generate new QuestPlayer
		if (!PlayerData.loadPlayerData(player)) {
			Main.plugin.questPlayer.put(player.getUniqueId(), new QuestPlayer(player));
			Main.log(String.format("New PlayerData file will be generated for: %s", player.getName()));
		}

		
		if (Main.plugin.questPlayer.containsKey(player.getUniqueId())) {
			QuestPlayer questPlayer = Main.plugin.questPlayer.get(player.getUniqueId());
			// Outputting 100 example quests in console (balancing purpose)
			for (int i = 0; i < 100; i++) {
				try {
					Quest q = QuestGenerator.generate(questPlayer);

					Main.log(q.getInfo(true));

				} catch (QuestGenerationException e) {
					Main.log(e.message);
					e.printStackTrace();
				}
			}
		}
	}
}
