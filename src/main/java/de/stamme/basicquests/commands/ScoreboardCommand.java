package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardCommand extends BasicQuestsCommand {
    public ScoreboardCommand() {
        super("scoreboard");
    }

    @Override
    public final @NotNull String getPermission() {
        return "basicquests.use.scoreboard";
    }

    @Override
    public void complete(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 1 || !(sender instanceof Player) || Config.isScoreboardDisabled()) {
            return;
        }
        // quests scoreboard ...
        List<String> possible = new ArrayList<>();

        if (QuestsScoreBoardManager.isBoardShowing((Player) sender)) {
            possible.add("hide");
        } else {
            possible.add("show");
        }
        possible.add("rewards");

        suggestByParameter(possible.stream(), suggestions, params.get(params.size() - 1));
    }

    @Override
    public void evaluate(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        if (params.size() > 1 || !(sender instanceof Player)) {
            return;
        }

        QuestPlayer questPlayer = plugin.getQuestPlayer((Player) sender);
        if (questPlayer == null) return;

        if (Config.isScoreboardDisabled()) {
            BasicQuestsPlugin.sendMessage(sender,  MessagesConfig.getMessage("commands.scoreboard.disabled"));
            return;
        }

        if (params.size() == 0) {
            if (QuestsScoreBoardManager.isBoardShowing(questPlayer.getPlayer())) {
                QuestsScoreBoardManager.hide(questPlayer);
                BasicQuestsPlugin.sendMessage(questPlayer.getPlayer(), MessagesConfig.getMessage("commands.scoreboard.hide"));
            } else {
                QuestsScoreBoardManager.show(questPlayer, false);
                BasicQuestsPlugin.sendMessage(questPlayer.getPlayer(), MessagesConfig.getMessage("commands.scoreboard.show"));
            }

            return;
        }

        if (params.get(0).equalsIgnoreCase("show")) {
            QuestsScoreBoardManager.show(questPlayer, false);
            BasicQuestsPlugin.sendMessage(questPlayer.getPlayer(), MessagesConfig.getMessage("commands.scoreboard.show"));
        } else if (params.get(0).equalsIgnoreCase("hide")) {
            QuestsScoreBoardManager.hide(questPlayer);
            BasicQuestsPlugin.sendMessage(questPlayer.getPlayer(), MessagesConfig.getMessage("commands.scoreboard.hide"));
        } else if (params.get(0).equalsIgnoreCase("rewards")) {
            QuestsScoreBoardManager.show(questPlayer, true);
            BasicQuestsPlugin.sendMessage(questPlayer.getPlayer(), MessagesConfig.getMessage("commands.scoreboard.show"));
        }
    }
}
