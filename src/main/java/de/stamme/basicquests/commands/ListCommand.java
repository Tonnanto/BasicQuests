package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.util.StringFormatter;
import de.themoep.minedown.MineDown;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public class ListCommand extends BasicQuestsCommand {

    protected ListCommand() {
        super("list");
    }

    @Override
    public final @NotNull String getPermission() {
        return "basicquests.use.list";
    }

    public final @NotNull String getAdminPermission() {
        return "basicquests.admin.list";
    }

    @Override
    public void complete(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params,
            @NotNull List<String> suggestions) {

        if (params.size() > 1) {
            return;
        }
        // quests list ...
        List<String> possible = new ArrayList<>();

        if (params.size() == 1 && sender.hasPermission(getAdminPermission())) {
            for (Player p : BasicQuestsPlugin.getPlugin().getServer().getOnlinePlayers()) {
                possible.add(p.getName());
            }
        }

        possible.add("rewards");

        suggestByParameter(possible.stream(), suggestions, params.get(params.size() - 1));
    }

    @Override
    public void evaluate(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        boolean showRewards = false;
        String targetPlayerName = null;

        if (params.size() == 1) {
            if (params.get(0).equals("rewards")) {
                // /quests list rewards
                showRewards = true;
            } else {
                // /quests list <player>
                showRewards = true;
                targetPlayerName = params.get(0);
            }
        }

        // List other player's quests
        if (targetPlayerName != null) {
            onListQuestsForOther(sender, targetPlayerName);
            return;
        }

        // List own quests
        if (!(sender instanceof Player player)) {
            sendNoQuestsFoundMessage(sender);
            return;
        }

        QuestPlayer questPlayer = plugin.getQuestPlayers().get(player.getUniqueId());

        if (questPlayer == null || questPlayer.getQuests().isEmpty()) {
            if (questPlayer != null && questPlayer.shouldShowHintForMoreQuestsTomorrow()) {
                BasicQuestsPlugin.sendMessage(sender,
                        MessageFormat.format(MessagesConfig.getMessage("commands.list.limit-reached"), StringFormatter.timeToNextDailyReset()));
            } else {
                sendNoQuestsFoundMessage(sender);
            }
            return;
        }

        if (showRewards) {
            sendQuestDetailMessage(questPlayer);
        } else {
            sendQuestsMessage(questPlayer);
        }
    }

    private void onListQuestsForOther(CommandSender sender, String targetName) {
        // check permission
        if (!sender.hasPermission(getAdminPermission())) {
            BasicQuestsPlugin.sendMessage(sender, MessagesConfig.getMessage("generic.no-permission"));
            return;
        }

        // Find the targeted quest player
        QuestPlayer targetPlayer = findTargetPlayer(sender, targetName);
        if (targetPlayer == null) {
            return;
        }

        sendQuestMessageForOther(targetPlayer, sender);
    }

    /**
     * Finds a QuestPlayer based on the given name
     *
     * @param sender the CommandSender who executed the command
     * @param targetName the name of the targeted player
     * @return the found QuestPlayer or null
     */
    @Nullable
    private QuestPlayer findTargetPlayer(CommandSender sender, String targetName) {
        targetName = MineDown.escape(targetName);

        Player target = BasicQuestsPlugin.getPlugin().getServer().getPlayer(targetName);

        if (target == null) {
            BasicQuestsPlugin.sendMessage(sender, MessageFormat.format(MessagesConfig.getMessage("generic.player-not-found"), targetName));
            return null;
        }

        // Check if targeted player is QuestPlayer
        QuestPlayer targetPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(target);

        if (targetPlayer == null) {
            BasicQuestsPlugin.sendMessage(sender, MessageFormat.format(MessagesConfig.getMessage("generic.player-not-found"), targetName));
            return null;
        }

        return targetPlayer;
    }

    String buildBasicQuestInfoMessage(int questNumber, Quest quest) {
        return quest.getInfo(questNumber, false, true);
    }

    /**
     * sends a message that no quests have been found for this player
     *
     * @param sender the sender to send this message to
     */
    void sendNoQuestsFoundMessage(CommandSender sender) {
        BasicQuestsPlugin.sendMessage(sender, MessagesConfig.getMessage("commands.list.none"));
    }

    /**
     * sends a message containing a list of all active quests. also shows a button that triggers
     * /quests list rewards
     *
     * @param questPlayer the player to send this message to
     */
    void sendQuestsMessage(QuestPlayer questPlayer) {
        questPlayer.sendRawMessage(MessagesConfig.getMessage("commands.list.header"));

        for (int i = 0; i < questPlayer.getQuests().size(); i++) {
            Quest quest = questPlayer.getQuests().get(i);
            questPlayer.sendRawMessage(buildBasicQuestInfoMessage(i + 1, quest));
        }

        if (questPlayer.shouldShowHintForMoreQuestsTomorrow()) {
            questPlayer
                    .sendRawMessage(MessageFormat.format(MessagesConfig.getMessage("commands.list.more-quests-tomorrow"), questPlayer.getQuests().size() + 1));
        }

        questPlayer.sendRawMessage(MessagesConfig.getMessage("commands.list.footer"));
    }

    /**
     * sends a message containing a list of all active quests as well as their rewards.
     *
     * @param questPlayer the player to send this message to
     */
    void sendQuestDetailMessage(QuestPlayer questPlayer) {
        StringBuilder message = new StringBuilder(MessagesConfig.getMessage("commands.list.header-rewards") + "\n");

        for (int i = 0; i < questPlayer.getQuests().size(); i++) {
            Quest q = questPlayer.getQuests().get(i);

            if (i != 0) {
                message.append("\n");
            }

            message.append(q.getInfo(i + 1, true, true));
        }

        if (questPlayer.shouldShowHintForMoreQuestsTomorrow()) {
            message.append("\n");
            message.append(MessageFormat.format(MessagesConfig.getMessage("commands.list.more-quests-tomorrow"), questPlayer.getQuests().size() + 1));
        }

        questPlayer.sendRawMessage(message.toString());
    }

    /**
     * sends a message containing a list of all active quests as well as their rewards for another
     * player.
     *
     * @param questPlayer the player whose quests to list.
     * @param sender the target to send this message to
     */
    void sendQuestMessageForOther(QuestPlayer questPlayer, CommandSender sender) {
        StringBuilder message = new StringBuilder(MessageFormat.format(MessagesConfig.getMessage("commands.list.header-player"), questPlayer.getName()) + "\n");

        for (int i = 0; i < questPlayer.getQuests().size(); i++) {
            Quest q = questPlayer.getQuests().get(i);

            if (i != 0) {
                message.append("\n");
            }

            message.append(q.getInfo(i + 1, true, true));
        }

        if (questPlayer.shouldShowHintForMoreQuestsTomorrow()) {
            message.append("\n");
            message.append(MessageFormat.format(MessagesConfig.getMessage("commands.list.more-quests-tomorrow"), questPlayer.getQuests().size() + 1));
        }

        BasicQuestsPlugin.sendRawMessage(sender, message.toString());
    }
}
