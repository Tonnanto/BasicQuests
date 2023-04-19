package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.config.MessagesConfig;
import de.themoep.minedown.MineDown;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CompleteCommand extends BasicQuestsCommand {
    public CompleteCommand() {
        super("complete");
    }

    @Override
    public final @NotNull String getPermission() {
        return "basicquests.admin.complete";
    }

    @Override
    public void complete(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 2 || !sender.hasPermission(getPermission())) {
            return;
        }

        // quests complete ...
        List<String> possible = new ArrayList<>();

        if (params.size() == 1 && sender.hasPermission(getPermission() + ".others")) {
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
            onConsoleCompleteQuest(sender, params);
            return;
        }

        // Command executed by player
        Player player = (Player) sender;
        @Nullable QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayers().get(player.getUniqueId());//;.getQuestPlayer(player);

        if (argsLen == 0) {
            // Player -> /quests complete

            if (questPlayer == null)
                return;

            // Prompt to select own quest in chat
            promptCompleteSelection(player, questPlayer, null);
            return;
        }

        if (argsLen == 1) {
            // Check argument
            try {
                int index = Integer.parseInt(params.get(0)) - 1;
                if (questPlayer == null)
                    return;

                // Player completing his own quest by index
                // QuestPlayer -> /quests complete [index]
                onCompleteQuestByIndex(sender, questPlayer, index, clicked, clickedQuestID);
                return;

            } catch (NumberFormatException ignored) {
                // Player completing others quest
                // Player -> /quests complete <Player>

                String targetName = params.get(0);
                onCompleteQuestForOther(sender, targetName, clicked, clickedQuestID, null);
                return;
            }
        }

        if (argsLen == 2) {
            // Player -> /quests complete <player> [index]

            // Check arguments
            int questIndex;
            try {
                questIndex = Integer.parseInt(params.get(1)) - 1;
            } catch (NumberFormatException ignored) {
                return;
            }

            String targetName = params.get(0);
            onCompleteQuestForOther(sender, targetName, clicked, clickedQuestID, questIndex);
        }
    }

    /**
     * Called when the /quests complete command has not been executed by player via chat.
     *
     * @param sender the CommandSender who executed the command
     * @param params the arguments of the command
     */
    private void onConsoleCompleteQuest(CommandSender sender, List<String> params) {
        if (params.size() != 2) {
            BasicQuestsPlugin.sendRawMessage(sender, MessageFormat.format(MessagesConfig.getMessage("generic.usage"), "completequest [player] [index]"));
            return;
        }

        // Console -> /quests complete <player> [index]

        // check permission
        if (!sender.hasPermission(getPermission() + ".others")) {
            BasicQuestsPlugin.sendRawMessage(sender,  MessagesConfig.getMessage("generic.no-permission"));
            return;
        }

        // Check arguments
        int index;
        try {
            index = Integer.parseInt(params.get(1)) - 1;
        } catch (NumberFormatException ignored) {
            BasicQuestsPlugin.sendRawMessage(sender, MessageFormat.format(MessagesConfig.getMessage("generic.usage"), "completequest [player] [index]"));
            return;
        }

        // Look for target QuestPlayer
        String targetName = params.get(0);
        @Nullable QuestPlayer targetPlayer = findTargetPlayer(sender, targetName);
        if (targetPlayer == null) return;

        targetPlayer.completeQuest(index, sender);
    }

    /**
     * Called when a CommandSender tries to complete a QuestPlayers quest by it's index
     * sender and target can be the same player!
     * sender -> /quests complete <target> [questIndex]
     *
     * @param sender the CommandSender who executed the command
     * @param target the QuestPlayer who's quest should be completed
     * @param questIndex the index of the quest that should be completed
     * @param clicked whether the sender has clicked on the chat to complete the quest
     * @param clickedQuestID the ID of the clicked quest.
     */
    private void onCompleteQuestByIndex(CommandSender sender, QuestPlayer target, int questIndex, boolean clicked, @Nullable String clickedQuestID) {
        // Check if the clicked quest is the quest at the given index
        if (target.getQuests().size() > questIndex) {
            String questID = target.getQuests().get(questIndex).getId();
            if (clicked && (questID == null || !questID.equals(clickedQuestID))) {
                BasicQuestsPlugin.sendMessage(sender,  MessagesConfig.getMessage("commands.complete.already-completed"));
                return;
            }
        }

        target.completeQuest(questIndex, sender);
    }

    /**
     * Called when a CommandSender tries to complete a players quest
     * sender -> /quests complete <target> [questIndex]
     * sender -> /quests complete <target>
     *
     * @param sender the CommandSender who executed the command
     * @param targetName the player who's quest should be completed
     * @param clicked whether the sender has clicked on the chat to complete the quest
     * @param clickedQuestID the ID of the clicked quest.
     * @param questIndex the index of the quest that should be completed
     */
    private void onCompleteQuestForOther(CommandSender sender, String targetName, boolean clicked, @Nullable String clickedQuestID, @Nullable Integer questIndex) {
        // check permission
        if (!sender.hasPermission(getPermission() + ".others")) {
            BasicQuestsPlugin.sendMessage(sender,  MessagesConfig.getMessage("generic.no-permission"));
            return;
        }

        // Find the targeted quest player
        QuestPlayer targetPlayer = findTargetPlayer(sender, targetName);
        if (targetPlayer == null)
            return;

        if (questIndex != null) {
            // Select other players quest by index
            onCompleteQuestByIndex(sender, targetPlayer, questIndex, clicked, clickedQuestID);
            return;
        }

        if (sender instanceof Player) {
            // Select other players quest in chat
            promptCompleteSelection((Player) sender, targetPlayer, targetName);
        }
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
            BasicQuestsPlugin.sendMessage(sender,  MessageFormat.format(MessagesConfig.getMessage("generic.player-not-found"), targetName));
            return null;
        }

        // Check if targeted player is QuestPlayer
        QuestPlayer targetPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(target);

        if (targetPlayer == null) {
            BasicQuestsPlugin.sendMessage(sender,  MessageFormat.format(MessagesConfig.getMessage("generic.player-not-found"), targetName));
            return null;
        }

        return targetPlayer;
    }

    /**
     * Shows a list of all possible quests to skip for the given player.
     * Prompts the sender to select a quest by clicking it in the chat.
     * A ClickEvent will be fired if a quest is clicked.
     * This event will execute another /quests complete command with the index.
     *
     * @param player the player to be prompted
     * @param target the players who's quest should be skipped
     * @param targetNameArgument the targets name to put in the new command. Null if selector and target are the same player.
     */
    public void promptCompleteSelection(Player player, QuestPlayer target, @Nullable String targetNameArgument) {
        BasicQuestsPlugin.sendRawMessage(
            player,
            player == target.getPlayer() ?
                MessagesConfig.getMessage("commands.complete.header") :
                MessageFormat.format(MessagesConfig.getMessage("commands.complete.header-other"), target.getName())
        );

        for (int i = 0; i < target.getQuests().size(); i++) {
            Quest quest = target.getQuests().get(i);

            if (quest.getId() == null) {
                quest.setId(UUID.randomUUID().toString());
            }

            BasicQuestsPlugin.sendRawMessage(
                player,
                MessageFormat.format(
                    MessagesConfig.getMessage("commands.complete.format"),
                    quest.getInfo(i+1, false, false),
                    targetNameArgument != null ? targetNameArgument + " " : "",
                    i + 1,
                    quest.getId()
                )
            );
        }

        BasicQuestsPlugin.sendRawMessage(
            player,
            player == target.getPlayer() ?
                MessagesConfig.getMessage("commands.complete.footer") :
                MessageFormat.format(MessagesConfig.getMessage("commands.complete.footer-other"), target.getName())
        );
    }
}
