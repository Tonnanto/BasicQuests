package de.stamme.basicquests;

import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.wrapper.BukkitVersion;
import de.stamme.basicquests.config.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.mockito.MockedStatic;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MockServer {

    public static void init() {

        // Mock Bukkit
        MockedStatic<Bukkit> mockedStaticBukkit = mockStatic(Bukkit.class);
        ItemFactory itemFactory = mock(ItemFactory.class);
        PotionMeta potionMeta = mock(PotionMeta.class);
        ItemMeta itemMeta = mock(ItemMeta.class);
//        doCallRealMethod().when(potionMeta).setBasePotionData(any(PotionData.class));
//        when(potionMeta.getBasePotionData()).thenReturn(mock(PotionData.class));
        when(itemFactory.getItemMeta(any())).thenReturn(itemMeta);
        // TODO Mock Potion Metadata ofr Potion rewards to be considered
//        when(itemFactory.getItemMeta(Material.POTION)).thenReturn(potionMeta);
//        when(itemFactory.getItemMeta(Material.SPLASH_POTION)).thenReturn(potionMeta);
//        when(itemFactory.getItemMeta(Material.LINGERING_POTION)).thenReturn(potionMeta);
        mockedStaticBukkit.when(Bukkit::getItemFactory).thenReturn(itemFactory);

        // Mock Server
        Server server = mock(Server.class);
        when(server.getPlayer(anyString())).thenAnswer(invocation -> {
            QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(UUID.fromString(String.valueOf(invocation.getArguments()[0])));
            if (questPlayer != null)
                return questPlayer.getPlayer();
            else
                return null;
        });

        // Mock Main (BasicQuests)
        MockedStatic<BasicQuestsPlugin> mockedStaticMain = mockStatic(BasicQuestsPlugin.class);
        BasicQuestsPlugin basicQuestsPlugin = mock(BasicQuestsPlugin.class);
        mockedStaticMain.when(BasicQuestsPlugin::getPlugin).thenReturn(basicQuestsPlugin);
        mockedStaticMain.when(BasicQuestsPlugin::getBukkitVersion).thenReturn(BukkitVersion.v1_19);
        when(MessagesConfig.getMessage(anyString())).thenCallRealMethod();

        Map<UUID, QuestPlayer> questPlayerMap = new HashMap<>();
        when(basicQuestsPlugin.getQuestPlayers()).thenReturn(questPlayerMap);
        when(basicQuestsPlugin.getQuestPlayer(any(Player.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Player player = (Player) args[0];
            return questPlayerMap.get(player.getUniqueId());
        });
        when(basicQuestsPlugin.getQuestPlayer(any(UUID.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return questPlayerMap.get((UUID) args[0]);
        });
        when(basicQuestsPlugin.getDataFolder()).thenReturn(new File("src/main/resources/"));

        when(basicQuestsPlugin.getServer()).thenReturn(server);

        mockConfig();
    }

    private static void mockConfig() {

        MockedStatic<Config> mockedConfig = mockStatic(Config.class);

        mockedConfig.when(Config::getSkipsPerDay).thenReturn(3);
        mockedConfig.when(Config::getQuestAmount).thenReturn(3);
        mockedConfig.when(Config::getMoneyFactor).thenReturn(1.0);
        mockedConfig.when(Config::getQuantityFactor).thenReturn(1.0);
        mockedConfig.when(Config::getRewardFactor).thenReturn(1.0);
        mockedConfig.when(Config::moneyRewards).thenReturn(true);
        mockedConfig.when(Config::xpRewards).thenReturn(true);
        mockedConfig.when(Config::itemRewards).thenReturn(true);
    }
}
