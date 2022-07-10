package de.stamme.basicquests;

import de.stamme.basicquests.data.Config;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class MockServer {

    public static void init() {

        // Mock Bukkit
        mockStatic(Bukkit.class);

        // Mock Server
        Server server = mock(Server.class);
        when(server.getPlayer(anyString())).thenAnswer(invocation -> {
            QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer(UUID.fromString(String.valueOf(invocation.getArguments()[0])));
            if (questPlayer != null)
                return questPlayer.getPlayer();
            else
                return null;
        });

        // Mock Main (BasicQuests)
        MockedStatic<Main> mockedStaticMain = mockStatic(Main.class);
        Main main = mock(Main.class);
        mockedStaticMain.when(Main::getPlugin).thenReturn(main);
        Map<UUID, QuestPlayer> questPlayerMap = new HashMap<>();
        when(main.getQuestPlayers()).thenReturn(questPlayerMap);
        when(main.getQuestPlayer(any(Player.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Player player = (Player) args[0];
            return questPlayerMap.get(player.getUniqueId());
        });
        when(main.getQuestPlayer(any(UUID.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return questPlayerMap.get((UUID) args[0]);
        });

        when(main.getServer()).thenReturn(server);

        mockConfig();
    }

    private static void mockConfig() {

        MockedStatic<Config> mockedConfig = mockStatic(Config.class);

        mockedConfig.when(Config::getSkipsPerDay).thenReturn(3);
        mockedConfig.when(Config::getQuestAmount).thenReturn(3);
        mockedConfig.when(Config::getMoneyFactor).thenReturn(1.0);
        mockedConfig.when(Config::getQuantityFactor).thenReturn(1.0);
        mockedConfig.when(Config::getRewardFactor).thenReturn(1.0);
    }

    private static void mockRewardGenerator() {

    }

}
