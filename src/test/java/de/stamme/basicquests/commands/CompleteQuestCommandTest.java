package de.stamme.basicquests.commands;

import de.stamme.basicquests.MockPlayers;
import de.stamme.basicquests.MockServer;
import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class CompleteQuestCommandTest {

    CommandSender consoleSender;
    CommandSender playerSender;

    QuestPlayer otherTargetPlayer;
    QuestPlayer sameTargetPlayer;

    @BeforeAll
    static void setUpPlugin() {
        MockServer.init();
    }

    @BeforeEach
    public void setUpCommand() {
        // Mock Players
        Player player1 = MockPlayers.getPlayer();
        QuestPlayer questPlayer1 = MockPlayers.getQuestPlayer(player1, 3);
        Main.getPlugin().getQuestPlayers().put(player1.getUniqueId(), questPlayer1);

        Player player2 = MockPlayers.getPlayer();
        QuestPlayer questPlayer2 = MockPlayers.getQuestPlayer(player2, 3);
        Main.getPlugin().getQuestPlayers().put(player2.getUniqueId(), questPlayer2);

        consoleSender = mock(ConsoleCommandSender.class);
        playerSender = player1;
        sameTargetPlayer = questPlayer1;
        otherTargetPlayer = questPlayer2;
    }

    @Test
    public void canPlayerCompleteQuest() {

        // Grant permission
        when(playerSender.hasPermission("quests.complete")).thenReturn(true);

        // Mock Command
        PluginCommand pluginCommand = mock(PluginCommand.class);
        CompleteQuestCommand completeQuestCommand = new CompleteQuestCommand();
        completeQuestCommand.onCommand(playerSender, pluginCommand, "label", new String[]{"1"});

        // Verify if quest has been completed
        Quest questToComplete = sameTargetPlayer.getQuests().get(0);
        Mockito.verify(sameTargetPlayer, Mockito.atLeastOnce()).completeQuest(0, playerSender);
        int completionProgress = questToComplete.getGoal();
        Mockito.verify(questToComplete, Mockito.atLeastOnce()).progress(completionProgress, sameTargetPlayer);
    }

    @Test
    public void canPlayerWithoutPermissionNotCompleteQuest() {

        // Mock Command
        PluginCommand pluginCommand = mock(PluginCommand.class);
        CompleteQuestCommand completeQuestCommand = new CompleteQuestCommand();
        completeQuestCommand.onCommand(playerSender, pluginCommand, "label", new String[]{"1"});

        // Verify if quest has NOT been completed
        Quest questToComplete = sameTargetPlayer.getQuests().get(0);
        Mockito.verify(sameTargetPlayer, Mockito.never()).completeQuest(0, playerSender);
        Mockito.verify(questToComplete, Mockito.never()).progress(anyInt(), eq(sameTargetPlayer));
    }

    @Test
    public void canPlayerCompleteOtherPlayersQuest() {

        // Grant permission
        when(playerSender.hasPermission("quests.complete")).thenReturn(true);
        when(playerSender.hasPermission("quests.complete.forothers")).thenReturn(true);

        // Mock Command
        PluginCommand pluginCommand = mock(PluginCommand.class);
        CompleteQuestCommand completeQuestCommand = new CompleteQuestCommand();
        completeQuestCommand.onCommand(playerSender, pluginCommand, "label", new String[]{otherTargetPlayer.getName(), "1"});

        // Verify if other players quest has been completed
        Quest questToComplete = otherTargetPlayer.getQuests().get(0);
        Mockito.verify(otherTargetPlayer, Mockito.atLeastOnce()).completeQuest(0, playerSender);
        int completionProgress = questToComplete.getGoal();
        Mockito.verify(questToComplete, Mockito.atLeastOnce()).progress(completionProgress, otherTargetPlayer);
    }

    @Test
    public void canConsoleCompleteOtherPlayersQuest() {

        // Grant permission
        when(consoleSender.hasPermission("quests.complete")).thenReturn(true);
        when(consoleSender.hasPermission("quests.complete.forothers")).thenReturn(true);

        // Mock Command
        PluginCommand pluginCommand = mock(PluginCommand.class);
        CompleteQuestCommand completeQuestCommand = new CompleteQuestCommand();
        completeQuestCommand.onCommand(consoleSender, pluginCommand, "label", new String[]{otherTargetPlayer.getName(), "1"});

        // Verify if other players quest has been completed
        Quest questToComplete = otherTargetPlayer.getQuests().get(0);
        Mockito.verify(otherTargetPlayer, Mockito.atLeastOnce()).completeQuest(0, consoleSender);
        int completionProgress = questToComplete.getGoal();
        Mockito.verify(questToComplete, Mockito.atLeastOnce()).progress(completionProgress, otherTargetPlayer);
    }

}
