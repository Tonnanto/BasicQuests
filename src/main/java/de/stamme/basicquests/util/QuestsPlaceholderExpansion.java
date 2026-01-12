package de.stamme.basicquests.util;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.ServerInfo;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.rewards.Reward;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class QuestsPlaceholderExpansion extends PlaceholderExpansion {
    private final BasicQuestsPlugin plugin;

    public QuestsPlaceholderExpansion(BasicQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bquests";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Tonanto";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String param) {

        QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(player.getUniqueId());
        if (questPlayer == null) {
            return null;
        }

        // %quests%
        if (param.equalsIgnoreCase("")) {
            return questPlayer.getQuestsMessage();
        }

        // %quests_count%
        if (param.equalsIgnoreCase("count")) {
            return String.valueOf(questPlayer.getQuests().size());
        }

        // %quests_completed%
        if (param.equalsIgnoreCase("completed")) {
            return String.valueOf(ServerInfo.getInstance().getCompletedQuestCountFor(questPlayer));
        }

        // %quests_detail%
        if (param.equalsIgnoreCase("detail")) {
            return questPlayer.getQuestsWithRewardsMessage();
        }

        String[] params = param.split("_");

        // %quests_top_1%
        if (params[0].equalsIgnoreCase("top")) {
            try {
                int leaderboardPos = Integer.parseInt(params[1]);
                List<Map.Entry<UUID, Integer>> leaderboard = ServerInfo.getInstance().getStarsLeaderboardSorted();

                if (leaderboard.size() >= leaderboardPos) {
                    Map.Entry<UUID, Integer> leaderboardEntry = leaderboard.get(leaderboardPos - 1);
                    String playerName = BasicQuestsPlugin.getPlugin().getServer().getOfflinePlayer(leaderboardEntry.getKey()).getName();
                    return leaderboardString(playerName, leaderboardPos, leaderboardEntry.getValue());
                }
                return leaderboardString(null, leaderboardPos, 0);
            } catch (NumberFormatException ignored) {
            }

            return null;
        }

        // %quests_1..%
        try {
            int questIndex = Integer.parseInt(params[0]) - 1;

            if (params.length == 1) {
                // %quests_1%
                String[] questLines = questPlayer.getQuestsMessage().split("\n");
                if (questLines.length <= questIndex) {
                    return null;
                }
                return questLines[questIndex];

            } else if (params[1].equalsIgnoreCase("title")) {
                // %quests_1_title%
                return questPlayer.getQuests().get(questIndex).getName();

            } else if (params[1].equalsIgnoreCase("progress")) {
                // %quests_1_progress%
                return questPlayer.getQuests().get(questIndex).getProgressString();

            } else if (params[1].equalsIgnoreCase("left")) {
                // %quests_1_left
                return questPlayer.getQuests().get(questIndex).getLeftString();

            } else if (params.length == 2) {
                int lineIndex = Integer.parseInt(params[1]) - 1;
                // %quests_1_1%
                String[] questTitleLines = questTitleLines(questPlayer.getQuests().get(questIndex));
                return questTitleLines[lineIndex];

            } else if (params.length == 3) {
                int lineIndex = Integer.parseInt(params[2]) - 1;
                if (params[1].equalsIgnoreCase("reward")) {
                    // %quests_1_reward_1%
                    String[] rewardLines = questRewardLines(questPlayer.getQuests().get(questIndex).getReward());
                    return rewardLines[lineIndex];
                }
            }

        } catch (NumberFormatException ignored) {
        }

        return null;
    }

    /**
     * Splits the title of a quest + the progress status into 4 lines of 14 or fewer characters
     * Built to display quests on signs.
     *
     * @param quest the quest to display
     * @return an array of four lines
     */
    private String[] questTitleLines(Quest quest) {
        int currentLine = 0;
        String[] questTitleLines = {"", "", "", ""};
        String[] questTitleWords = quest.getName().split(" ");
        StringBuilder nextLine = new StringBuilder();

        for (String word : questTitleWords) {
            if (currentLine == 4) {
                break;
            }
            if (nextLine.length() == 0) {
                if (word.length() > 15) {
                    nextLine.append(word, 0, 13).append("..");
                } else {
                    nextLine.append(word);
                }
            } else if (nextLine.length() + word.length() < 14) {
                nextLine.append(" ").append(word);
            } else {
                questTitleLines[currentLine] = nextLine.toString();
                if (word.length() > 15) {
                    nextLine = new StringBuilder().append(word, 0, 13).append("..");
                } else {
                    nextLine = new StringBuilder(word);
                }

                currentLine++;
            }
        }

        if (currentLine < 4) {
            questTitleLines[currentLine] = nextLine.toString();
        }

        String progressString = MessageFormat.format(MessagesConfig.getMessage("placeholder.quest-progress"), quest.getProgressString());

        if (questTitleLines[3].length() + progressString.length() <= 14) {
            questTitleLines[3] += progressString;
        }

        return questTitleLines;
    }

    /**
     * Splits the rewards of a quest into two lines Uses "+ 2 more.." If more than two rewards items
     *
     * @param reward the reward to display
     * @return an array of three lines
     */
    private String[] questRewardLines(Reward reward) {
        String[] rewardLines = {"", "", "", ""};

        switch (reward.getRewardType()) {
            case XP :
                rewardLines[0] = reward.xpString(false);
                break;
            case MONEY :
                rewardLines[0] = reward.moneyString(false);
                break;
            case ITEM :
                String[] itemRewardLines = reward.itemString(false).split("\n");

                if (itemRewardLines.length > 1) {
                    rewardLines[0] = itemRewardLines[1];
                }

                if (itemRewardLines.length > 2) {
                    rewardLines[1] = itemRewardLines[2];
                }

                if (itemRewardLines.length > 3) {
                    rewardLines[1] += " " + MessageFormat.format(MessagesConfig.getMessage("placeholder.more"), (itemRewardLines.length - 3));
                }

                break;
        }

        return rewardLines;
    }

    /**
     * Returns a String that represents one line on the leaderboard
     *
     * @param name the player's name
     * @param pos the player's position on the board
     * @param stars the number of stars the player has earned
     * @return one line for the leaderboard
     */
    private String leaderboardString(String name, int pos, int stars) {
        if (name == null || name.isEmpty()) {
            return MessageFormat.format(MessagesConfig.getMessage("placeholder.leaderboard.empty-line"), pos);
        }
        return MessageFormat.format(MessagesConfig.getMessage("placeholder.leaderboard.line"), pos, name, StringFormatter.starString(stars, true));
    }
}
