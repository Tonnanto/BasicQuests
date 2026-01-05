package de.stamme.basicquests.commands;

import de.stamme.basicquests.model.QuestPlayer;
import org.bukkit.command.CommandSender;

public class CompleteQuestCommandTest {

  CommandSender consoleSender;
  CommandSender playerSender;

  QuestPlayer otherTargetPlayer;
  QuestPlayer sameTargetPlayer;

  //    @BeforeAll
  //    static void setUpPlugin() {
  //        MockServer.init();
  //    }
  //
  //    @BeforeEach
  //    public void setUpCommand() {
  //        // Mock Players
  //        Player player1 = MockPlayers.getPlayer();
  //        QuestPlayer questPlayer1 = MockPlayers.getQuestPlayer(player1, 3);
  //        BasicQuestsPlugin.getPlugin().getQuestPlayers().put(player1.getUniqueId(),
  // questPlayer1);
  //
  //        Player player2 = MockPlayers.getPlayer();
  //        QuestPlayer questPlayer2 = MockPlayers.getQuestPlayer(player2, 3);
  //        BasicQuestsPlugin.getPlugin().getQuestPlayers().put(player2.getUniqueId(),
  // questPlayer2);
  //
  //        consoleSender = mock(ConsoleCommandSender.class);
  //        playerSender = player1;
  //        sameTargetPlayer = questPlayer1;
  //        otherTargetPlayer = questPlayer2;
  //    }
  //
  //    @Test
  //    public void canPlayerCompleteQuest() {
  //
  //        // Grant permission
  //        when(playerSender.hasPermission("basicquests.admin.complete")).thenReturn(true);
  //
  //        // Mock Command
  //        PluginCommand pluginCommand = mock(PluginCommand.class);
  //        CompleteQuestCommand completeQuestCommand = new CompleteQuestCommand();
  //        completeQuestCommand.onCommand(playerSender, pluginCommand, "label", new String[]{"1"});
  //
  //        // Verify if quest has been completed
  //        Quest questToComplete = sameTargetPlayer.getQuests().get(0);
  //        Mockito.verify(sameTargetPlayer, Mockito.atLeastOnce()).completeQuest(0, playerSender);
  //        int completionProgress = questToComplete.getGoal();
  //        Mockito.verify(questToComplete, Mockito.atLeastOnce()).progress(completionProgress,
  // sameTargetPlayer);
  //    }
  //
  //    @Test
  //    public void canPlayerWithoutPermissionNotCompleteQuest() {
  //
  //        // Mock Command
  //        PluginCommand pluginCommand = mock(PluginCommand.class);
  //        CompleteQuestCommand completeQuestCommand = new CompleteQuestCommand();
  //        completeQuestCommand.onCommand(playerSender, pluginCommand, "label", new String[]{"1"});
  //
  //        // Verify if quest has NOT been completed
  //        Quest questToComplete = sameTargetPlayer.getQuests().get(0);
  //        Mockito.verify(sameTargetPlayer, Mockito.never()).completeQuest(0, playerSender);
  //        Mockito.verify(questToComplete, Mockito.never()).progress(anyInt(),
  // eq(sameTargetPlayer));
  //    }
  //
  //    @Test
  //    public void canPlayerCompleteOtherPlayersQuest() {
  //
  //        // Grant permission
  //        when(playerSender.hasPermission("basicquests.admin.complete")).thenReturn(true);
  //        when(playerSender.hasPermission("basicquests.admin.complete.others")).thenReturn(true);
  //
  //        // Mock Command
  //        PluginCommand pluginCommand = mock(PluginCommand.class);
  //        CompleteQuestCommand completeQuestCommand = new CompleteQuestCommand();
  //        completeQuestCommand.onCommand(playerSender, pluginCommand, "label", new
  // String[]{otherTargetPlayer.getName(), "1"});
  //
  //        // Verify if other players quest has been completed
  //        Quest questToComplete = otherTargetPlayer.getQuests().get(0);
  //        Mockito.verify(otherTargetPlayer, Mockito.atLeastOnce()).completeQuest(0, playerSender);
  //        int completionProgress = questToComplete.getGoal();
  //        Mockito.verify(questToComplete, Mockito.atLeastOnce()).progress(completionProgress,
  // otherTargetPlayer);
  //    }
  //
  //    @Test
  //    public void canConsoleCompleteOtherPlayersQuest() {
  //
  //        // Grant permission
  //        when(consoleSender.hasPermission("basicquests.admin.complete")).thenReturn(true);
  //        when(consoleSender.hasPermission("basicquests.admin.complete.others")).thenReturn(true);
  //
  //        // Mock Command
  //        PluginCommand pluginCommand = mock(PluginCommand.class);
  //        CompleteQuestCommand completeQuestCommand = new CompleteQuestCommand();
  //        completeQuestCommand.onCommand(consoleSender, pluginCommand, "label", new
  // String[]{otherTargetPlayer.getName(), "1"});
  //
  //        // Verify if other players quest has been completed
  //        Quest questToComplete = otherTargetPlayer.getQuests().get(0);
  //        Mockito.verify(otherTargetPlayer, Mockito.atLeastOnce()).completeQuest(0,
  // consoleSender);
  //        int completionProgress = questToComplete.getGoal();
  //        Mockito.verify(questToComplete, Mockito.atLeastOnce()).progress(completionProgress,
  // otherTargetPlayer);
  //    }

}
