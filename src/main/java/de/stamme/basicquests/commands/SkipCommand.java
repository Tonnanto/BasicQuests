package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.util.StringFormatter;
import de.themoep.minedown.MineDown;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkipCommand extends BasicQuestsCommand {
    public SkipCommand() {
        super("skip");
    }

    @Override
    public final @NotNull String getPermission() {
        return "basicquests.use.skip";
    }

    @Override
    public void complete(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 2 || !sender.hasPermission(getPermission())) {
            return;
        }
        // quests skip ...
        List<String> possible = new ArrayList<>();
        if (params.size() == 1 && sender.hasPermission("basicquests.admin.skip.others")) {
            for (Player p: BasicQuestsPlugin.getPlugin().getServer().getOnlinePlayers()) {
                possible.add(p.getName());
            }
        }

        int questAmount = Config.getQuestAmount();
        for (int i = 1; i <= questAmount; i++) {
            possible.add("" + i);
        }

        suggestByParameter(possible.stream(), suggestions, params.get(params.size() - 1));
    }

    @Override
    public void evaluate(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        // Popping last two arguments if the command was executed through a ClickEvent in the chat
        int argsLen = params.size();
        boolean clicked = false;

        String clickedQuestID = "";

        if (params.size() > 1 && params.get(params.size() - 2).equalsIgnoreCase("clicked")) {
            clicked = true;
            clickedQuestID = params.get(params.size() - 1);
            argsLen -= 2;
        }

        if (!(sender instanceof Player)) {
            // Command executed by console
            onConsoleSkipQuest(sender, params);
            return;
        }

        // Command executed by player
        Player player = (Player) sender;
        @Nullable QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(player);

        if (argsLen == 0) {
            // Player -> /quests skip
            if (questPlayer == null) {
                return;
            }

            // Check skip / permission
            int skipsLeft = Config.getSkipsPerDay() - questPlayer.getSkipCount();
            if (skipsLeft <= 0 && !sender.hasPermission("basicquests.admin.skip.unlimited")) {
                questPlayer.sendMessage(MessageFormat.format(MessagesConfig.getMessage("commands.skip.none"), StringFormatter.timeToMidnight()));
                return;
            }

            // Prompt to select own quest in chat
            promptSkipSelection(player, questPlayer, null);
            return;
        }

        if (argsLen == 1) {
            // Check argument
            try {
                int index = Integer.parseInt(params.get(0)) - 1;
                if (questPlayer == null) return;

                // Check skips / permission
                int skipsLeft = Config.getSkipsPerDay() - questPlayer.getSkipCount();
                if (skipsLeft <= 0 && !sender.hasPermission("basicquests.admin.skip.unlimited")) {
                    questPlayer.sendMessage(MessageFormat.format(MessagesConfig.getMessage("commands.skip.none"), StringFormatter.timeToMidnight()));
                    return;
                }

                // Player skipping his own quest by index
                // QuestPlayer -> /quests skip [index]
                onSkipQuestByIndex(sender, questPlayer, index, clicked, clickedQuestID);
                return;

            } catch (NumberFormatException ignored) {
                // Player skipping others quest
                // Player -> /quests skip <Player>

                String targetName = params.get(0);
                onSkipQuestForOther(sender, targetName, clicked, clickedQuestID, null);
                return;
            }
        }

        if (argsLen == 2) {
            // Player -> /quests skip <player> [index]

            // Check arguments
            int questIndex;
            try {
                questIndex = Integer.parseInt(params.get(1)) - 1;
            } catch (NumberFormatException ignored) {
                return;
            }

            String targetName = params.get(0);
            onSkipQuestForOther(sender, targetName, clicked, clickedQuestID, questIndex);
        }
    }

    /**
     * Called when the /quests skip command has not been executed by player via chat.
     * @param sender the CommandSender who executed the command
     * @param params the arguments of the command
     */
    private void onConsoleSkipQuest(CommandSender sender, List<String> params) {
        if (params.size() != 2) {
            BasicQuestsPlugin.sendRawMessage(sender, MessageFormat.format(MessagesConfig.getMessage("generic.usage"), "skipquest [player] [index]"));
            return;
        }

        // Console -> /quests skip <player> [index]

        // check permission
        if (!sender.hasPermission("basicquests.admin.skip.others")) {
            BasicQuestsPlugin.sendRawMessage(sender,  MessagesConfig.getMessage("generic.no-permission"));
            return;
        }

        // Check arguments
        int index;
        try {
            index = Integer.parseInt(params.get(1)) - 1;
        } catch (NumberFormatException ignored) {
            BasicQuestsPlugin.sendRawMessage(sender, MessageFormat.format(MessagesConfig.getMessage("generic.usage"), "skipquest [player] [index]"));
            return;
        }

        // Look for target QuestPlayer
        String targetName = params.get(0);
        @Nullable QuestPlayer targetPlayer = findTargetPlayer(sender, targetName);
        if (targetPlayer == null) return;

        targetPlayer.skipQuest(index, sender);
    }

    /**
     * Called when a CommandSender tries to skip a QuestPlayers quest by it's index
     * sender and target can be the same player!
     * sender -> /quests skip <target> [questIndex]
     *
     * @param sender the CommandSender who executed the command
     * @param target the QuestPlayer who's quest should be skipped
     * @param questIndex the index of the quest that should be skipped
     * @param clicked whether the sender has clicked on the chat to skip the quest
     * @param clickedQuestID the ID of the clicked quest.
     */
    private void onSkipQuestByIndex(CommandSender sender, QuestPlayer target, int questIndex, boolean clicked, @Nullable String clickedQuestID) {
        if (target.getQuests().size() > questIndex) {
            String questID = target.getQuests().get(questIndex).getId();

            if (clicked && (questID == null || !questID.equals(clickedQuestID))) {
                BasicQuestsPlugin.sendMessage(sender,  MessagesConfig.getMessage("commands.skip.already-skipped"));
                return;
            }
        }

        target.skipQuest(questIndex, sender);
    }

    /**
     * Called when a CommandSender tries to skip a players quest
     * sender -> /quests skip <target> [questIndex]
     * sender -> /quests skip <target>
     *
     * @param sender the CommandSender who executed the command
     * @param targetName the player who's quest should be skipped
     * @param clicked whether the sender has clicked on the chat to skip the quest
     * @param clickedQuestID the ID of the clicked quest.
     * @param questIndex the index of the quest that should be skipped
     */
    private void onSkipQuestForOther(CommandSender sender, String targetName, boolean clicked, @Nullable String clickedQuestID, @Nullable Integer questIndex) {
        // check permission
        if (!sender.hasPermission("basicquests.admin.skip.others")) {
            BasicQuestsPlugin.sendMessage(sender,  MessagesConfig.getMessage("generic.no-permission"));
            return;
        }

        // Find the targeted quest player
        QuestPlayer targetPlayer = findTargetPlayer(sender, targetName);
        if (targetPlayer == null) return;

        if (questIndex != null) {
            // Select other players quest by index
            onSkipQuestByIndex(sender, targetPlayer, questIndex, clicked, clickedQuestID);
            return;
        }

        if (sender instanceof Player) {
            // Select other players quest in chat
            promptSkipSelection((Player) sender, targetPlayer, targetName);
        }
    }

    /**
     * Retrieve a QuestPlayer using their player name.
     *
     * @param  sender The command sender.
     * @param  targetName The target player.
     * @return QuestPlayer
     */
    @Nullable
    private QuestPlayer findTargetPlayer(CommandSender sender, String targetName) {
        targetName = MineDown.escape(targetName);

        Player target = BasicQuestsPlugin.getPlugin().getServer().getPlayer(targetName);

        if (target == null) {
            BasicQuestsPlugin.sendMessage(
                sender,
                MessageFormat.format(MessagesConfig.getMessage("generic.player-not-found"), targetName)
            );

            return null;
        }

        // Check if targeted player is QuestPlayer
        QuestPlayer targetPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(target);

        if (targetPlayer == null) {
            BasicQuestsPlugin.sendMessage(
                sender,
                MessageFormat.format(MessagesConfig.getMessage("generic.player-not-found"), targetName)
            );

            return null;
        }

        return targetPlayer;
    }

    /**
     * Shows a list of all possible quests to skip for the given player.
     *
     * @param player the player to be prompted
     * @param target the players whose quest should be skipped
     * @param targetNameArgument the targets name to put in the new command. Null if selector and target are the same player.
     */
    public void promptSkipSelection(Player player, QuestPlayer target, @Nullable String targetNameArgument) {
        BasicQuestsPlugin.sendRawMessage(
            player,
            player == target.getPlayer() ?
            MessagesConfig.getMessage("commands.skip.header") :
            MessagesConfig.getMessage("commands.skip.header-other")
        );

        for (int i = 0; i < target.getQuests().size(); i++) {
            Quest quest = target.getQuests().get(i);

            if (quest.getId() == null) {
                quest.setId(UUID.randomUUID().toString());
            }

            BasicQuestsPlugin.sendRawMessage(
                player,
                MessageFormat.format(
                    MessagesConfig.getMessage("commands.skip.format"),
                    quest.getInfo(false),
                    targetNameArgument != null ? targetNameArgument + " " : "",
                    i + 1,
                    quest.getId()
                )
            );
        }

        BasicQuestsPlugin.sendRawMessage(
            player,
            player == target.getPlayer() ?
                MessagesConfig.getMessage("commands.skip.footer") :
                MessageFormat.format(MessagesConfig.getMessage("commands.skip.footer-other"), target.getName())
        );

        if (
            target.getPlayer() == player &&
                !player.hasPermission("basicquests.admin.skip.unlimited") &&
                !player.hasPermission("basicquests.admin.skip.others")
        ) {
            ChoiceFormat skipsFormat = new ChoiceFormat(new double[]{0, 1, 2}, new String[]{
                MessagesConfig.getMessage("generic.skip.none"),
                MessagesConfig.getMessage("generic.skip.singular"),
                MessagesConfig.getMessage("generic.skip.plural"),
            });

            BasicQuestsPlugin.sendRawMessage(
                player,
                MessageFormat.format(
                    MessagesConfig.getMessage("commands.skip.remaining"),
                    target.getSkipsLeft(),
                    skipsFormat.format(target.getSkipsLeft())
                )
            );
        }
    }
}
