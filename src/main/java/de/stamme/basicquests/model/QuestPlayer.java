package de.stamme.basicquests.model;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.ServerInfo;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.model.generation.QuestGenerationException;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.quests.QuestData;
import de.stamme.basicquests.questgeneration.QuestGenerator;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import de.stamme.basicquests.util.StringFormatter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * The representation of a player in Basic Quests This player has a list of quests and a certain
 * number of skips per day
 */
public class QuestPlayer {

    // ---------------------------------------------------------------------------------------
    // Player State
    // ---------------------------------------------------------------------------------------

    private final Player player;
    private transient Inventory rewardInventory;

    private List<Quest> quests;
    private int skipTodayCount;
    private int completedTodayCount;

    // 0 - no
    // 1 - yes
    // 2 - yes with rewards
    private int showScoreboard;

    // ---------------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------------

    // new Player on the Server
    public QuestPlayer(Player player) {
        this.player = player;
        resetQuests();
    }

    public QuestPlayer(PlayerData data, Player player) {
        this.player = player;
        this.skipTodayCount = data.skipCount;
        this.completedTodayCount = data.completedCount;

        // build quest list
        List<Quest> questList = new ArrayList<>();
        for (QuestData questData : data.questSnapshot) {
            // Skip invalid quests so they get regenerated
            if (questData.isInvalid()) {
                continue;
            }

            Quest quest = questData.toQuest();
            if (quest != null) {
                questList.add(quest);
            }
        }
        this.quests = questList;

        receiveNewQuests(false);
    }

    // ---------------------------------------------------------------------------------------
    // Functionality
    // ---------------------------------------------------------------------------------------

    /** resets all of a players quests and the daily quest limit */
    public void resetQuests() {
        quests = new ArrayList<>();
        completedTodayCount = 0;

        receiveNewQuests(Config.announceQuestsWhenReset());
    }

    /**
     * Ensures the player has the configured amount of quests,
     * respecting the daily quest limit.
     *
     * @param announce whether newly added quests should be announced
     */
    public void receiveNewQuests(boolean announce) {
        quests.removeIf(Quest::isRewardReceived);

        int missing = Config.getQuestAmount() - quests.size();
        if (missing <= 0) {
            return;
        }

        missing = Math.min(missing, getQuestsLeftForToday());
        if (missing <= 0) {
            return;
        }

        addNewQuests(missing, announce);
        QuestsScoreBoardManager.refresh(this);
    }

    /**
     * Adds <amount> quests to the player.
     *
     * @param amount number of quests to add
     * @param announce whether to announce the new quests
     */
    private void addNewQuests(int amount, boolean announce) {
        if (amount <= 0) {
            return;
        }

        Quest[] questsToAnnounce = announce ? new Quest[amount] : null;
        int index = 0;

        for (int i = 0; i < amount; i++) {
            try {
                Quest quest = QuestGenerator.getInstance().generate(this);
                quests.add(quest);

                if (announce) {
                    questsToAnnounce[index++] = quest;
                }
            } catch (QuestGenerationException e) {
                BasicQuestsPlugin.log(Level.SEVERE, e.getMessage());
            }
        }

        if (announce && index > 0) {
            announceQuests(index == questsToAnnounce.length ? questsToAnnounce : Arrays.copyOf(questsToAnnounce, index));
        }
    }

    /**
     * skips a quest at a certain index
     *
     * @param index index of the quest to skip
     * @param sender the player who initiated the skip
     */
    public void skipQuest(int index, CommandSender sender) {
        if (getQuests() == null || getQuests().size() <= index || index < 0) {
            BasicQuestsPlugin.sendMessage(sender, MessageFormat.format(MessagesConfig.getMessage("commands.skip.not-found"), index + 1));
            return;
        }

        int skipsLeft = Config.getSkipsPerDay() - getSkipTodayCount();

        if (sender == getPlayer() && skipsLeft <= 0 && !hasPermission("basicquests.admin.skip.unlimited")) {
            sendMessage(MessageFormat.format(MessagesConfig.getMessage("commands.skip.none"), StringFormatter.timeToNextDailyReset()));
            return;
        }

        try {
            if (!hasPermission("basicquests.admin.skip.unlimited")) {
                if (sender == getPlayer()) {
                    increaseSkipCount();
                }

                String message = MessageFormat.format(MessagesConfig.getMessage("commands.skip.skipped"), index + 1);

                message += ChatColor.WHITE + " - ";

                if (getSkipsLeftForToday() <= 0) {
                    message += MessageFormat.format(MessagesConfig.getMessage("commands.skip.none"), StringFormatter.timeToNextDailyReset());
                } else {
                    message += MessageFormat.format(MessagesConfig.getMessage("commands.skip.remaining"), getSkipsLeftForToday(),
                            StringFormatter.formatSkips(getSkipsLeftForToday()));
                }

                sendMessage(message);
            } else {
                sendMessage(MessageFormat.format(MessagesConfig.getMessage("commands.skip.skipped"), index + 1));
            }

            if (sender != getPlayer()) {
                BasicQuestsPlugin.sendMessage(sender,
                        MessageFormat.format(MessagesConfig.getMessage("commands.skip.skipped-other"), getPlayer().getName(), index + 1));
            }

            // Remove Quest and add it to ServerInfo.skippedQuests
            Quest skippedQuest = getQuests().remove(index);
            if (!hasPermission("basicquests.admin.skip.unlimited")) {
                // Do not include skips of players with unlimited skips
                ServerInfo.getInstance().questSkipped(skippedQuest);
            }

            // Generate new Quest
            Quest newQuest = QuestGenerator.getInstance().generate(this);
            getQuests().add(index, newQuest);
            announceQuests(newQuest);
            QuestsScoreBoardManager.refresh(this);

        } catch (QuestGenerationException e) {
            BasicQuestsPlugin.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * completes a quest at a certain index
     *
     * @param index index of the quest to complete
     * @param sender the player who initiated the completion
     */
    public void completeQuest(int index, CommandSender sender) {
        if (getQuests() == null || getQuests().size() <= index || index < 0) {
            BasicQuestsPlugin.sendMessage(sender, MessageFormat.format(MessagesConfig.getMessage("commands.complete.not-found"), index + 1));
            return;
        }

        Quest quest = getQuests().get(index);

        if (quest.isCompleted()) {
            BasicQuestsPlugin.sendMessage(sender, MessagesConfig.getMessage("commands.complete.already-completed"));
            return;
        }

        quest.progress(quest.getGoal() * 100, this);

        if (sender == getPlayer()) {
            sendMessage(MessageFormat.format(MessagesConfig.getMessage("commands.complete.success"), index + 1));
            return;
        }

        BasicQuestsPlugin.sendMessage(sender,
                MessageFormat.format(MessagesConfig.getMessage("commands.complete.success-other"), getPlayer().getName(), index + 1));
    }

    /**
     * sends a message to the player announcing the given quests.
     *
     * @param quests quests to announce.
     */
    private void announceQuests(Quest... quests) {
        if (quests.length == 0) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(BasicQuestsPlugin.getPlugin(), () -> {
            player.sendMessage(quests.length == 1
                    ? MessagesConfig.getMessage("events.player.new-quest.singular")
                    : MessagesConfig.getMessage("events.player.new-quest.plural"));

            for (Quest quest : quests) {
                int questNumber = getQuests().indexOf(quest) + 1;
                BasicQuestsPlugin.sendRawMessage(player, quest.getInfo(questNumber, true, true));
            }
        }, 60L);
    }

    public void sendActionMessage(String message) {
        BasicQuestsPlugin.sendActionMessage(player, message);
    }

    public void sendMessage(String message) {
        BasicQuestsPlugin.sendMessage(player, message);
    }

    public void sendRawMessage(String message) {
        BasicQuestsPlugin.sendRawMessage(player, message);
    }

    // ---------------------------------------------------------------------------------------
    // Getter & Setter
    // ---------------------------------------------------------------------------------------

    public String getQuestsMessage() {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < getQuests().size(); i++) {
            Quest quest = getQuests().get(i);
            if (i != 0) {
                message.append("\n");
            }
            message.append(quest.getInfo(i + 1, false, false));
        }
        return message.toString();
    }

    public String getQuestsWithRewardsMessage() {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < getQuests().size(); i++) {
            Quest quest = getQuests().get(i);
            if (i != 0) {
                message.append("\n");
            }
            message.append("\n").append(quest.getInfo(i + 1, true, false));
        }
        return message.toString();
    }

    public String getName() {
        return player.getName();
    }

    public int getSkipTodayCount() {
        return skipTodayCount;
    }

    public int getSkipsLeftForToday() {
        return Config.getSkipsPerDay() - skipTodayCount;
    }

    public void setSkipTodayCount(int x) {
        skipTodayCount = x;
    }

    public void increaseSkipCount() {
        skipTodayCount++;
    }

    public int getCompletedTodayCount() {
        return completedTodayCount;
    }

    public int getQuestsLeftForToday() {
        int questsPerDay = Config.getQuestsPerDay();
        if (questsPerDay < 0 || hasPermission("basicquests.admin.receive.unlimited")) {
            return Integer.MAX_VALUE;
        }
        return questsPerDay - getCompletedTodayCount() - getQuests().size();
    }

    public void setCompletedTodayCount(int x) {
        completedTodayCount = x;
    }

    public void increaseCompletedCount() {
        completedTodayCount++;
    }

    public boolean shouldShowHintForMoreQuestsTomorrow() {
        return getQuests().size() < Config.getQuestAmount() && getQuestsLeftForToday() <= 0;
    }

    public boolean hasPermission(String key) {
        return player.hasPermission(key);
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getRewardInventory() {
        return rewardInventory;
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public void setRewardInventory(Inventory rewardInventory) {
        this.rewardInventory = rewardInventory;
    }

    public int getShowScoreboard() {
        return showScoreboard;
    }

    public void setShowScoreboard(int showScoreboard) {
        this.showScoreboard = Math.min(showScoreboard, 2);
    }
}
