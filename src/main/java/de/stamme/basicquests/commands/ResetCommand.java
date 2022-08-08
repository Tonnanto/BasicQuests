package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.PlayerData;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.config.MessagesConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ResetCommand extends BasicQuestsCommand {
    protected ResetCommand() {
        super("reset");
    }

    @Override
    public void complete(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 1 || (getPermission() != null && !sender.hasPermission(getPermission()))) {
            return;
        }
        // quests reset ...
        List<String> possible = new ArrayList<>();
        if (sender.hasPermission(getPermission() + ".global")) {
            possible.add("global");
        }
        if (sender.hasPermission(getPermission() + ".forothers")) {
            for (Player p: BasicQuestsPlugin.getPlugin().getServer().getOnlinePlayers()) {
                possible.add(p.getName());
            }
        }
        suggestByParameter(possible.stream(), suggestions, params.get(params.size() - 1));
    }

    @Override
    public void evaluate(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {

        if (params.size() > 0) {
            if (params.size() > 1) return;
            if (params.get(0).equalsIgnoreCase("global")) {
                resetGlobally(plugin, sender);
                return;
            }
            // "/quests reset <player>"
            resetForOtherPlayer(plugin, sender, params.get(0));

        } else if (sender instanceof Player) {
            // "/quests reset"
            QuestPlayer questPlayer = plugin.getQuestPlayer((Player) sender);
            if (questPlayer == null) {
                String errorMessage = MessagesConfig.getMessage("generic.not-found.player");

                BasicQuestsPlugin.log(errorMessage);
                BasicQuestsPlugin.sendMessage(sender, errorMessage);

                return;
            }
            resetForSelf(questPlayer);
        }
    }

    /**
     * Rests quests for all players
     * @param sender the sender of the command
     */
    void resetGlobally(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender) {
        if (!sender.hasPermission(getPermission() + ".global")) {
            BasicQuestsPlugin.sendMessage(sender,  MessagesConfig.getMessage("generic.no-permission"));
            return;
        }

        for (QuestPlayer target : plugin.getQuestPlayers().values()) {
            target.resetQuests();
            target.sendMessage(MessagesConfig.getMessage("quests.questsHaveBeenReset"));
        }

        for (OfflinePlayer offlinePlayer: plugin.getServer().getOfflinePlayers()) {
            PlayerData.resetQuestsForOfflinePlayer(offlinePlayer);
        }
    }

    /**
     * Tries to reset the quests of another player
     * @param sender the sender of the command
     * @param targetName the name of the player whos quests to reset
     */
    void resetForOtherPlayer(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, String targetName) {
        Player targetPlayer = plugin.getServer().getPlayer(targetName);

        if (targetPlayer != sender && !sender.hasPermission("basicquests.reset.forothers")) {
            BasicQuestsPlugin.sendMessage(sender,  MessagesConfig.getMessage("generic.no-permission"));
            return;
        }

        QuestPlayer target = plugin.getQuestPlayer(targetPlayer);

        if (target == null) {
            BasicQuestsPlugin.sendMessage(sender,  MessageFormat.format(MessagesConfig.getMessage("generic.not-found.player"), targetName));
            return;
        }

        target.resetQuests();
        target.sendMessage(MessagesConfig.getMessage("quests.questsHaveBeenReset"));
    }

    /**
     * Tries to reset the quests for the player himself
     * @param questPlayer the player who wants to reset his quests
     */
    void resetForSelf(QuestPlayer questPlayer) {
        questPlayer.resetQuests();
        questPlayer.sendMessage(MessagesConfig.getMessage("quests.questsHaveBeenReset"));
    }
}
