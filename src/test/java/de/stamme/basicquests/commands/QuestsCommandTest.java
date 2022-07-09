package de.stamme.basicquests.commands;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.BlockBreakQuest;
import de.stamme.basicquests.quests.ChopWoodQuest;
import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.quests.Reward;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Mockito.*;

public class QuestsCommandTest {

    @Test
    public void isMessageSent() {

        // Mock Spigot Player
        Player.Spigot spigotPlayer = mock(Player.Spigot.class);

        // Mock Player
        UUID playerUUID = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Player 1");
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.spigot()).thenReturn(spigotPlayer);

        // Mock Quest Player
        List<Quest> quests = new ArrayList<>();
        quests.add(new ChopWoodQuest("LOG", 32, new Reward()));

        QuestPlayer questPlayer = mock(QuestPlayer.class);
        when(questPlayer.getPlayer()).thenReturn(player);
        when(questPlayer.getQuests()).thenReturn(quests);

        // Mock Main (BasicQuests)
        MockedStatic<Main> mockedStaticMain = mockStatic(Main.class);
        Main main = mock(Main.class);
        mockedStaticMain.when(Main::getPlugin).thenReturn(main);
        Map<UUID, QuestPlayer> questPlayerMap = new HashMap<>();
        questPlayerMap.put(playerUUID, questPlayer);
        when(main.getQuestPlayers()).thenReturn(questPlayerMap);

        // Mock Command
        PluginCommand pluginCommand = mock(PluginCommand.class);
        QuestsCommand questsCommand = new QuestsCommand();
        questsCommand.onCommand(player, pluginCommand, "label", null);

        // Verify if message has been sent
        String expectedQuestMessage = questsCommand.buildBasicQuestInfoMessage(quests.get(0));
        Mockito.verify(questPlayer, Mockito.atLeastOnce()).sendMessage(expectedQuestMessage);
    }
}
