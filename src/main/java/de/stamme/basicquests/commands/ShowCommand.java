package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.config.MessagesConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class ShowCommand extends BasicQuestsCommand {

    protected ShowCommand() {
        super("show");
    }

    @Override
    public void complete(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {

        if (params.size() > 1) {
            return;
        }
        // quests show ...
        List<String> possible = new ArrayList<>();

        possible.add("rewards");

        suggestByParameter(possible.stream(), suggestions, params.get(params.size() - 1));
    }

    @Override
    public void evaluate(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        if (!(sender instanceof Player)) return;

        QuestPlayer questPlayer = plugin.getQuestPlayer((Player) sender);
        if (questPlayer == null) {
            sender.sendMessage(ChatColor.RED + MessagesConfig.getMessage("quests.noQuestsFound"));
            return;
        } // is QuestPlayer

        if (questPlayer.getQuests().size() <= 0) {
            sender.sendMessage(ChatColor.RED + MessagesConfig.getMessage("quests.noQuestsFound"));
            return;
        } // QuestPlayer has Quests

        // "/quests show"
        if (params.size() == 0) {
            sendQuestsMessage(questPlayer);
            return;
        }

        // "/quests show rewards"
        if (params.size() == 1 && params.get(0).equals("rewards")) {
            sendQuestDetailMessage(questPlayer);
        }
    }


    String buildBasicQuestInfoMessage(Quest quest) {
        return String.format("%s>%s %s", ChatColor.GOLD, ChatColor.WHITE, quest.getInfo(false));
    }

    /**
     * sends a message containing a list of all active quests.
     * also shows a button that triggers /quests detail
     * @param questPlayer the player to send this message to
     */
    void sendQuestsMessage(QuestPlayer questPlayer) {
        ComponentBuilder message = new ComponentBuilder("\n" + MessagesConfig.getMessage("quests.yourQuests") + ":  ");
        TextComponent showRewardsButton = new TextComponent(ChatColor.AQUA + ">> " + MessagesConfig.getMessage("rewards.showRewards") + " <<");
        showRewardsButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MessagesConfig.getMessage("rewards.clickToShowRewardsTooltip"))));
        showRewardsButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests detail"));
        message.append(showRewardsButton);
        questPlayer.getPlayer().spigot().sendMessage(message.create());

        for (Quest quest: questPlayer.getQuests()) {
            questPlayer.sendMessage(buildBasicQuestInfoMessage(quest));
        }
    }

    /**
     * sends a message containing a list of all active quests as well as their rewards.
     * @param questPlayer the player to send this message to
     */
    void sendQuestDetailMessage(QuestPlayer questPlayer) {
        StringBuilder message = new StringBuilder("\n" + MessagesConfig.getMessage("quests.questsAndRewards") + ":");
        for (int i = 0; i < questPlayer.getQuests().size(); i++) {
            Quest q = questPlayer.getQuests().get(i);
            if (i != 0)
                message.append("\n ");

            message.append(String.format("\n%s>%s %s", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(true)));
        }
        questPlayer.sendMessage(message.toString());
    }
}
