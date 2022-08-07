package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.PlayerData;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.util.L10n;
import net.md_5.bungee.api.ChatColor;
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
        suggestByParameter(possible.stream(), suggestions, params.get(0));
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
                String errorMessage = L10n.getMessage("commands.questPlayerNotFound");
                BasicQuestsPlugin.log(errorMessage);
                sender.sendMessage(ChatColor.RED + errorMessage);
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
            sender.sendMessage(ChatColor.RED + L10n.getMessage("commands.commandNotAllowed"));
            return;
        }

        for (QuestPlayer target : plugin.getQuestPlayers().values()) {
            target.resetQuests();
            target.sendMessage(ChatColor.GREEN + L10n.getMessage("quests.questsHaveBeenReset"));
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
            sender.sendMessage(ChatColor.RED + L10n.getMessage("commands.actionNotAllowed"));
            return;
        }

        QuestPlayer target = plugin.getQuestPlayer(targetPlayer);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + MessageFormat.format(L10n.getMessage("commands.playerNotFound"), targetName));
            return;
        }

        target.resetQuests();
        target.sendMessage(ChatColor.GREEN + L10n.getMessage("quests.questsHaveBeenReset"));
    }

    /**
     * Tries to reset the quests for the player himself
     * @param questPlayer the player who wants to reset his quests
     */
    void resetForSelf(QuestPlayer questPlayer) {
        questPlayer.resetQuests();
        questPlayer.sendMessage(ChatColor.GREEN + L10n.getMessage("quests.questsHaveBeenReset"));
    }
}
