package de.stamme.basicquests.listeners;

import de.stamme.basicquests.data.Config;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quest_generation.QuestGenerator;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class PlayerJoinListenerTest {


    // TODO: WIP
////    @Test
//    public void isMessageSent() throws Exception {
//        // Mock Player
//        UUID playerUUID = UUID.randomUUID();
//        Player player = mock(Player.class);
//        when(player.getName()).thenReturn("Player 1");
//        when(player.getUniqueId()).thenReturn(playerUUID);
//
//        // Mock Join Event
//        PlayerJoinEvent event = mock(PlayerJoinEvent.class);
//        when(event.getPlayer()).thenReturn(player);
//
//        // Mock Main (BasicQuests)
//        MockedStatic<Main> mockedStaticMain = mockStatic(Main.class);
//        Main main = mock(Main.class);
//        mockedStaticMain.when(Main::getPlugin).thenReturn(main);
//        QuestPlayer questPlayer = main.getQuestPlayers(playerUUID);
//
//        // Mock Config
//        MockedStatic<Config> mockedStaticConfig = mockStatic(Config.class);
//        mockedStaticConfig.when(Config::getQuestAmount).thenReturn(3);
//
//        // Mock QuestGenerator
//        MockedStatic<QuestGenerator> mockedStaticQuestGenerator = mockStatic(QuestGenerator.class);
////        mockedStaticQuestGenerator.when(QuestGenerator::generate).thenReturn(new );
//
//
//        // Fire Join Event
//        PlayerJoinListener listener = new PlayerJoinListener();
//        listener.onPlayerJoin(event);
//
//
//        // has player been added to servers
//    }
}