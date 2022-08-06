package de.stamme.basicquests.commands;

public class QuestsCommandTest {

//    @Test
//    public void isMessageSent() {
//
//        // Mock Spigot Player
//        Player.Spigot spigotPlayer = mock(Player.Spigot.class);
//
//        // Mock Player
//        UUID playerUUID = UUID.randomUUID();
//        Player player = mock(Player.class);
//        when(player.getName()).thenReturn("Player 1");
//        when(player.getUniqueId()).thenReturn(playerUUID);
//        when(player.spigot()).thenReturn(spigotPlayer);
//
//        // Mock Quest Player
//        List<Quest> quests = new ArrayList<>();
//        quests.add(new ChopWoodQuest("LOG", 32, new Reward()));
//
//        QuestPlayer questPlayer = mock(QuestPlayer.class);
//        when(questPlayer.getPlayer()).thenReturn(player);
//        when(questPlayer.getQuests()).thenReturn(quests);
//
//        // Mock Main (BasicQuests)
//        MockedStatic<BasicQuestsPlugin> mockedStaticMain = mockStatic(BasicQuestsPlugin.class);
//        BasicQuestsPlugin basicQuestsPlugin = mock(BasicQuestsPlugin.class);
//        mockedStaticMain.when(BasicQuestsPlugin::getPlugin).thenReturn(basicQuestsPlugin);
//        Map<UUID, QuestPlayer> questPlayerMap = new HashMap<>();
//        questPlayerMap.put(playerUUID, questPlayer);
//        when(basicQuestsPlugin.getQuestPlayers()).thenReturn(questPlayerMap);
//
//        // Mock Command
//        PluginCommand pluginCommand = mock(PluginCommand.class);
//        QuestsCommand questsCommand = new QuestsCommand();
//        questsCommand.onCommand(player, pluginCommand, "label", null);
//
//        // Verify if message has been sent
//        String expectedQuestMessage = questsCommand.buildBasicQuestInfoMessage(quests.get(0));
//        Mockito.verify(questPlayer, Mockito.atLeastOnce()).sendMessage(expectedQuestMessage);
//    }
}
