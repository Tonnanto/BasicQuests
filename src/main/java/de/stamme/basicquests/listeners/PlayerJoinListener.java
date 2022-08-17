package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.model.PlayerData;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import de.stamme.basicquests.util.UpdateChecker;
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
			QuestPlayer joinedPlayer = new QuestPlayer(player);
			BasicQuestsPlugin.getPlugin().getQuestPlayers().put(player.getUniqueId(), joinedPlayer);

			if (Config.showScoreboardPerDefault()) {
				QuestsScoreBoardManager.show(joinedPlayer, false);
			}
		}

		// Notify player if a new version is available
		if (player.hasPermission("basicquests.admin.update")) {
		    BasicQuestsPlugin.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(BasicQuestsPlugin.getPlugin(),
                () -> UpdateChecker.getInstance().notifyUser(player), 50L);
        }
	}
}
