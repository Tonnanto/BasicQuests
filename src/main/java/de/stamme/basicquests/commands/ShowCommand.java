package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.config.MessagesConfig;
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
            BasicQuestsPlugin.sendMessage(sender,  MessagesConfig.getMessage("quests.noQuestsFound"));
            return;
        } // is QuestPlayer

        if (questPlayer.getQuests().size() <= 0) {
            BasicQuestsPlugin.sendMessage(sender,  MessagesConfig.getMessage("quests.noQuestsFound"));
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
        return String.format("> %s", quest.getInfo(false));
    }

    /**
     * sends a message containing a list of all active quests.
     * also shows a button that triggers /quests show rewards
     *
     * @param questPlayer the player to send this message to
     */
    void sendQuestsMessage(QuestPlayer questPlayer) {
        questPlayer.sendMessage("Your Quests: [>> Show Rewards <<](hover=" + MessagesConfig.getMessage("rewards.clickToShowRewardsTooltip") + " run_command=/quests show rewards)");

        for (Quest quest: questPlayer.getQuests()) {
            questPlayer.sendRawMessage(buildBasicQuestInfoMessage(quest));
        }
    }

    /**
     * sends a message containing a list of all active quests as well as their rewards.
     *
     * @param questPlayer the player to send this message to
     */
    void sendQuestDetailMessage(QuestPlayer questPlayer) {
        StringBuilder message = new StringBuilder("\n" + MessagesConfig.getMessage("quests.questsAndRewards") + ":");
        for (int i = 0; i < questPlayer.getQuests().size(); i++) {
            Quest q = questPlayer.getQuests().get(i);
            if (i != 0)
                message.append("\n ");

            message.append(String.format("\n> %s", q.getInfo(true)));
        }
        questPlayer.sendMessage(message.toString());
    }
}
