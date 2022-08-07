package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.Config;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.util.L10n;
import de.stamme.basicquests.util.StringFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
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
    public void complete(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 2 || (getPermission() != null && !sender.hasPermission(getPermission()))) {
            return;
        }
        // quests skip ...
        List<String> possible = new ArrayList<>();
        if (params.size() == 1 && sender.hasPermission(getPermission() + ".forothers")) {
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
        if (params.size() > 1 && params.get(params.size() - 2).equalsIgnoreCase("CLICKED")) {
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

            if (questPlayer == null) return;

            // Check skips / permission
            int skipsLeft = Config.getSkipsPerDay() - questPlayer.getSkipCount();
            if (skipsLeft <= 0 && !sender.hasPermission("basicquests.skip.unlimited")) {
                questPlayer.sendMessage(ChatColor.RED + MessageFormat.format(L10n.getMessage("player.noSkipsLeftInfo"), StringFormatter.timeToMidnight()));
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
                if (skipsLeft <= 0 && !sender.hasPermission("basicquests.skip.unlimited")) {
                    questPlayer.sendMessage(ChatColor.RED + MessageFormat.format(L10n.getMessage("player.noSkipsLeftInfo"), StringFormatter.timeToMidnight()));
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
            sender.sendMessage(MessageFormat.format(L10n.getMessage("commands.usage"), "skipquest [player] [index]"));
            return;
        }

        // Console -> /quests skip <player> [index]

        // check permission
        if (!sender.hasPermission("basicquests.skip.forothers")) {
            sender.sendMessage(ChatColor.RED + L10n.getMessage("commands.actionNotAllowed"));
            return;
        }

        // Check arguments
        int index;
        try {
            index = Integer.parseInt(params.get(1)) - 1;
        } catch (NumberFormatException ignored) {
            sender.sendMessage(MessageFormat.format(L10n.getMessage("commands.usage"), "skipquest [player] [index]"));
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
     * @param sender the CommandSender who executed the command
     * @param target the QuestPlayer who's quest should be skipped
     * @param questIndex the index of the quest that should be skipped
     * @param clicked whether the sender has clicked on the chat to skip the quest
     * @param clickedQuestID the ID of the clicked quest.
     */
    private void onSkipQuestByIndex(CommandSender sender, QuestPlayer target, int questIndex, boolean clicked, @Nullable String clickedQuestID) {
        // Check if the clicked quest is the quest at the given index
        if (target.getQuests().size() > questIndex) {
            String questID = target.getQuests().get(questIndex).getId();
            if (clicked && (questID == null || !questID.equals(clickedQuestID))) {
                sender.sendMessage(ChatColor.RED + L10n.getMessage("commands.questAlreadySkipped"));
                return;
            }
        }

        target.skipQuest(questIndex, sender);
    }


    /**
     * Called when a CommandSender tries to skip a players quest
     * sender -> /quests skip <target> [questIndex]
     * sender -> /quests skip <target>
     * @param sender the CommandSender who executed the command
     * @param targetName the player who's quest should be skipped
     * @param clicked whether the sender has clicked on the chat to skip the quest
     * @param clickedQuestID the ID of the clicked quest.
     * @param questIndex the index of the quest that should be skipped
     */
    private void onSkipQuestForOther(CommandSender sender, String targetName, boolean clicked, @Nullable String clickedQuestID, @Nullable Integer questIndex) {
        // check permission
        if (!sender.hasPermission("basicquests.skip.forothers")) {
            sender.sendMessage(ChatColor.RED + L10n.getMessage("commands.actionNotAllowed"));
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
     * Finds a QuestPlayer based on the given name
     * @param sender the CommandSender who executed the command
     * @param targetName the name of the targeted player
     * @return the found QuestPlayer or null
     */
    @Nullable
    private QuestPlayer findTargetPlayer(CommandSender sender, String targetName) {

        // Check if targeted player is online
        Player target = BasicQuestsPlugin.getPlugin().getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + MessageFormat.format(L10n.getMessage("commands.playerNotFound"), targetName));
            return null;
        }

        // Check if targeted player is QuestPlayer
        QuestPlayer targetPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(target);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + L10n.getMessage("commands.questPlayerNotFound"));
            return null;
        }

        return targetPlayer;
    }

    /**
     * Shows a list of all possible quests to skip for the given player.
     * Prompts the sender to select a quest by clicking it in the chat.
     * A ClickEvent will be fired if a quest is clicked.
     * This event will execute another /quests skip command with the index.
     * @param selector the player to be prompted
     * @param target the players who's quest should be skipped
     * @param targetNameArgument the targets name to put in the new command. Null if selector and target are the same player.
     */
    public void promptSkipSelection(Player selector, QuestPlayer target, @Nullable String targetNameArgument) {

        if (selector == target.getPlayer()) {
            selector.sendMessage(ChatColor.AQUA + "\n" + L10n.getMessage("commands.clickQuestToSkip"));
        } else {
            selector.sendMessage(ChatColor.AQUA + "\n" + MessageFormat.format(L10n.getMessage("commands.clickQuestTSkipForOther"), target.getName()));
        }

        if (target.getPlayer() == selector && !selector.hasPermission("basicquests.skip.unlimited") && !selector.hasPermission("basicquests.skip.forothers")) {
            ChoiceFormat skipsFormat = new ChoiceFormat(new double[]{0, 1, 2}, new String[]{
                    L10n.getMessage("skip.none"),
                    L10n.getMessage("skip.singular"),
                    L10n.getMessage("skip.plural"),
            });
            selector.sendMessage(ChatColor.AQUA + MessageFormat.format(L10n.getMessage("player.skipsLeftInfo"), target.getSkipsLeft(), skipsFormat.format(target.getSkipsLeft())));
        }

        StringBuilder command = new StringBuilder("/quests skip");
        if (targetNameArgument != null) {
            command.append(" ").append(targetNameArgument);
        }

        for (int i = 0; i < target.getQuests().size(); i++) {
            Quest quest = target.getQuests().get(i);
            if (quest.getId() == null)
                quest.setId(UUID.randomUUID().toString());

            TextComponent questText = new TextComponent(String.format(" %s> %s%s", ChatColor.AQUA, ChatColor.UNDERLINE, quest.getInfo(false)));
            questText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(L10n.getMessage("commands.clickToSkipTooltip"))));
            questText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (i+1) + " CLICKED " + quest.getId()));

            selector.spigot().sendMessage(questText);
        }
    }
}
