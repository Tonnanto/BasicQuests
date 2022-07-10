package de.stamme.basicquests;

import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.ChopWoodQuest;
import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.quests.Reward;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

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

    public static QuestPlayer getQuestPlayer(Player player) {
        // Mock Quests
        Quest quest1 = MockQuests.getQuest();
        Quest quest2 = MockQuests.getQuest();
        Quest quest3 = MockQuests.getQuest();
        List<Quest> quests = new ArrayList<>(Arrays.asList(quest1, quest2, quest3));

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
