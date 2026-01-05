package de.stamme.basicquests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MockPlayers {

  public static Player getPlayer() {
    // Mock Spigot Player
    Player.Spigot spigotPlayer = mock(Player.Spigot.class);

    // Mock Player
    UUID playerUUID = UUID.randomUUID();
    Player player = mock(Player.class);
    when(player.getName()).thenReturn(String.valueOf(playerUUID));
    when(player.getUniqueId()).thenReturn(playerUUID);
    when(player.spigot()).thenReturn(spigotPlayer);

    return player;
  }

  public static QuestPlayer getQuestPlayer(Player player, int questCount) {
    // Mock Quests
    List<Quest> quests = new ArrayList<>();
    for (int i = 0; i < questCount; i++) {
      quests.add(MockQuests.getQuest());
    }

    // Mock Quest Player
    QuestPlayer questPlayer = mock(QuestPlayer.class);

    // Mock methods
    when(questPlayer.getPlayer()).thenReturn(player);
    when(questPlayer.getQuests()).thenReturn(quests);
    String playerName = player.getName();
    when(questPlayer.getName()).thenReturn(playerName);

    // Real Methods
    doCallRealMethod().when(questPlayer).completeQuest(anyInt(), any(CommandSender.class));
    doCallRealMethod().when(questPlayer).skipQuest(anyInt(), any(CommandSender.class));

    return questPlayer;
  }
}
