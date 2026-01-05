package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.PlayerData;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerQuitListener implements Listener {

  @EventHandler
  public void onPlayerQuit(@NotNull PlayerQuitEvent event) {

    QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(event.getPlayer());
    if (questPlayer == null) return;

    if (PlayerData.getPlayerDataAndSave(questPlayer))
      BasicQuestsPlugin.log("PlayerData Saved: " + questPlayer.getName());
    else BasicQuestsPlugin.log(Level.SEVERE, "Failed to save PlayerData: " + questPlayer.getName());

    QuestsScoreBoardManager.hide(questPlayer);
    BasicQuestsPlugin.getPlugin().getQuestPlayers().remove(questPlayer.getPlayer().getUniqueId());
  }
}
