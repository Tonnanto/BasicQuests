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

public class SkipQuestCommandTest {

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
    public void canPlayerSkipQuest() {

        // Grant permission
        when(playerSender.hasPermission("quests.skip")).thenReturn(true);

        // Mock Command
        PluginCommand pluginCommand = mock(PluginCommand.class);
        SkipQuestCommand skipQuestCommand = new SkipQuestCommand();
        skipQuestCommand.onCommand(playerSender, pluginCommand, "label", new String[]{"1"});

        // Verify if quest has been skipped
        Quest questToSkip = sameTargetPlayer.getQuests().get(0);
        Mockito.verify(sameTargetPlayer, Mockito.atLeastOnce()).skipQuest(0, playerSender);
        int completionProgress = questToSkip.getGoal();
        Mockito.verify(questToSkip, Mockito.atLeastOnce()).progress(completionProgress, sameTargetPlayer);
    }

    @Test
    public void canPlayerWithoutPermissionNotSkipQuest() {

        // Mock Command
        PluginCommand pluginCommand = mock(PluginCommand.class);
        SkipQuestCommand skipQuestCommand = new SkipQuestCommand();
        skipQuestCommand.onCommand(playerSender, pluginCommand, "label", new String[]{"1"});

        // Verify if quest has NOT been skipped
        Quest questToSkip = sameTargetPlayer.getQuests().get(0);
        Mockito.verify(sameTargetPlayer, Mockito.never()).skipQuest(0, playerSender);
        Mockito.verify(questToSkip, Mockito.never()).progress(anyInt(), eq(sameTargetPlayer));
    }

    @Test
    public void canPlayerSkipOtherPlayersQuest() {

        // Grant permission
        when(playerSender.hasPermission("quests.skip")).thenReturn(true);
        when(playerSender.hasPermission("quests.skip.forothers")).thenReturn(true);

        // Mock Command
        PluginCommand pluginCommand = mock(PluginCommand.class);
        SkipQuestCommand skipQuestCommand = new SkipQuestCommand();
        skipQuestCommand.onCommand(playerSender, pluginCommand, "label", new String[]{otherTargetPlayer.getName(), "1"});

        // Verify if other players quest has been skipped
        Quest questToSkip = otherTargetPlayer.getQuests().get(0);
        Mockito.verify(otherTargetPlayer, Mockito.atLeastOnce()).skipQuest(0, playerSender);
        int completionProgress = questToSkip.getGoal();
        Mockito.verify(questToSkip, Mockito.atLeastOnce()).progress(completionProgress, otherTargetPlayer);
    }

    @Test
    public void canConsoleSkipOtherPlayersQuest() {

        // Grant permission
        when(consoleSender.hasPermission("quests.skip")).thenReturn(true);
        when(consoleSender.hasPermission("quests.skip.forothers")).thenReturn(true);

        // Mock Command
        PluginCommand pluginCommand = mock(PluginCommand.class);
        SkipQuestCommand skipQuestCommand = new SkipQuestCommand();
        skipQuestCommand.onCommand(consoleSender, pluginCommand, "label", new String[]{otherTargetPlayer.getName(), "1"});

        // Verify if other players quest has been skipped
        Quest questToSkip = otherTargetPlayer.getQuests().get(0);
        Mockito.verify(otherTargetPlayer, Mockito.atLeastOnce()).skipQuest(0, consoleSender);
        int completionProgress = questToSkip.getGoal();
        Mockito.verify(questToSkip, Mockito.atLeastOnce()).progress(completionProgress, otherTargetPlayer);
    }
}
