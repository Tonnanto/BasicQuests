package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.ServerInfo;
import de.stamme.basicquests.config.MessagesConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderboardCommand extends BasicQuestsCommand {

    protected LeaderboardCommand() {
        super("leaderboard");
    }

    @Override
    public final @NotNull String getPermission() {
        return "basicquests.use.leaderboard";
    }

    @Override
    public void complete(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {

        if (params.size() > 1) {
            return;
        }
        // quests leaderboard ...
        List<String> possible = new ArrayList<>();

        possible.add("full");

        suggestByParameter(possible.stream(), suggestions, params.get(params.size() - 1));
    }

    @Override
    public void evaluate(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {

        // "/quests leaderboard"
        if (params.size() == 0) {
            sendLeaderboardMessage(sender);
            return;
        }

        // "/quests leaderboard full"
        if (params.size() == 1 && params.get(0).equals("full")) {
            sendFullLeaderboardMessage(sender);
        }
    }

    /**
     * sends a message containing a leaderboard of the top 5 players.
     *
     * @param sender the sender to send this message to
     */
    void sendLeaderboardMessage(CommandSender sender) {
        StringBuilder message = new StringBuilder(
            MessagesConfig.getMessage("commands.leaderboard.header") + "\n"
        );

        List<Map.Entry<UUID, Integer>> leaderboard = ServerInfo.getInstance().getQuestsLeaderboard();
        for (int i = 0; i < 5; i++) {

            if (i != 0) {
                message.append("\n");
            }

            if (leaderboard.size() <= i) {
                message.append(
                    leaderboardString(null, i + 1, 0)
                );
                continue;
            }

            Map.Entry<UUID, Integer> leaderboardEntry = leaderboard.get(i);
            String playerName = BasicQuestsPlugin.getPlugin().getServer().getOfflinePlayer(leaderboardEntry.getKey()).getName();

            message.append(
                leaderboardString(playerName, i + 1, leaderboardEntry.getValue())
            );
        }

        BasicQuestsPlugin.sendRawMessage(sender, message.toString());

        // Display button to show full leaderboard
        if (leaderboard.size() > 5) {
            BasicQuestsPlugin.sendRawMessage(sender, MessagesConfig.getMessage("commands.leaderboard.footer"));
        }
    }

    /**
     * sends a message containing a full leaderboard of all players.
     *
     * @param sender the sender to send this message to
     */
    void sendFullLeaderboardMessage(CommandSender sender) {
        StringBuilder message = new StringBuilder(
            MessagesConfig.getMessage("commands.leaderboard.header") + "\n"
        );

        List<Map.Entry<UUID, Integer>> leaderboard = ServerInfo.getInstance().getQuestsLeaderboard();
        for (int i = 0; i < leaderboard.size(); i++) {
            Map.Entry<UUID, Integer> leaderboardEntry = leaderboard.get(i);
            String playerName = BasicQuestsPlugin.getPlugin().getServer().getOfflinePlayer(leaderboardEntry.getKey()).getName();

            if (i != 0) {
                message.append("\n");
            }

            message.append(
                leaderboardString(playerName, i + 1, leaderboardEntry.getValue())
            );
        }

        BasicQuestsPlugin.sendRawMessage(sender, message.toString());
    }

    /**
     * Returns a String that represents one line on the leaderboard
     * @param name the players name
     * @param pos the players position on the board
     * @param questsCompleted the number of quests the player has completed
     * @return one line for the leaderboard
     */
    private String leaderboardString(String name, int pos, int questsCompleted) {
        if (name == null || name.isEmpty()) {
            return MessageFormat.format(MessagesConfig.getMessage("placeholder.leaderboard.empty-line"), pos);
        }
        return MessageFormat.format(MessagesConfig.getMessage("placeholder.leaderboard.line"), pos, name, questsCompleted);
    }
}
