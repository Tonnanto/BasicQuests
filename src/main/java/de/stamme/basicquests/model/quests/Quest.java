package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.ServerInfo;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.model.rewards.Reward;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;

import java.text.MessageFormat;
import java.util.Map;
import java.util.UUID;

abstract public class Quest {

	// ---------------------------------------------------------------------------------------
	// Quest State
	// ---------------------------------------------------------------------------------------

	private final int goal;
	private final Reward reward;
	private int count = 0;
	private boolean rewardReceived = false;
	private double value;

	// prevents wrong quests from being completed / skipped with a ClickEvent
	private transient String id;


	// ---------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------

	public Quest(int goal, Reward reward) {
		this.goal = goal;
		this.reward = reward;
	}


	// ---------------------------------------------------------------------------------------
	// Functionality
	// ---------------------------------------------------------------------------------------

	/**
	 * adds x to the Quest.count and notifies the player
 	 */
	public void progress(int x, QuestPlayer questPlayer) {
		if (count == goal) { return; }
		if (Config.isWorldBanned(questPlayer.getPlayer().getWorld().getName())) {
            questPlayer.sendActionMessage(
                MessagesConfig.getMessage("quest.progress.disabled-in-world")
            );
		    return;
        }
		count = Math.min(count + x, goal);

		// Notify player about progress
        // don't notify if progress is negative
		if (x >= 0) {
		    int questNumber = questPlayer.getQuests().indexOf(this) + 1;
            questPlayer.sendActionMessage(
                getInfo(questNumber, false)
            );
		}

		// Show title if Quest is completed
		if (isCompleted()) {
            broadcastOnCompletion(questPlayer);

			questPlayer.sendMessage(MessagesConfig.getMessage("events.player.receive-reward"));
			questPlayer.getPlayer().sendTitle(MessagesConfig.getMessage("events.player.quest-completed"), getName(), 10, 70, 20);

			if (Config.soundOnQuestCompletion()) {
				// Play Sound
				Location playerLocation = questPlayer.getPlayer().getLocation();
				questPlayer.getPlayer().playSound(playerLocation, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 10);
			}

            questPlayer.incrementCompletedQuests();
			ServerInfo.getInstance().questCompleted(this, questPlayer); // Add completed Quest to ServerInfo
        }

		QuestsScoreBoardManager.refresh(questPlayer);
	}

	private void broadcastOnCompletion(QuestPlayer questPlayer) {
	    String message = MessageFormat.format(
            MessagesConfig.getMessage("events.broadcast.quest-complete"),
            questPlayer.getPlayer().getName(),
            getName()
        );

        if (Config.broadcastOnQuestCompletion()) {
            // Broadcast to every player
            BasicQuestsPlugin.broadcastMessage(message);
        }

        // Log to console
        BasicQuestsPlugin.log(message);
	}

	/**
	 * Creates a QuestData Object from this Quest
	 * This Object contains this Quests state so it can be serialized and persisted.
	 */
	public QuestData toData() {
		QuestData data = new QuestData();

		data.setGoal(goal);
		data.setCount(count);
		data.setValue(value);
		data.setReward(reward);
		data.setRewardReceived(rewardReceived);

		return data;
	}

	/**
     * Looks for active Quests that require periodic checks for progress
     */
    public static void startProgressScheduler() {
        Bukkit.getScheduler().runTaskTimer(BasicQuestsPlugin.getPlugin(), () -> {
            for (Map.Entry<UUID, QuestPlayer> entry: BasicQuestsPlugin.getPlugin().getQuestPlayers().entrySet()) {
                QuestPlayer questPlayer = entry.getValue();
                if (questPlayer == null) continue;
                for (Quest quest: questPlayer.getQuests()) {
                    if (quest instanceof FindStructureQuest && !quest.isCompleted()) {
                        ((FindStructureQuest) quest).checkForProgress(questPlayer);
                    } else if (quest instanceof IncreaseStatQuest && !quest.isCompleted()) {
                        ((IncreaseStatQuest) quest).checkForProgress(questPlayer);
                    }
                }
            }
        }, 40L, 40L);
    }


	// ---------------------------------------------------------------------------------------
	// Getter & Setter
	// ---------------------------------------------------------------------------------------

	/**
	 * @return the description of the quest.
 	 */
	public abstract String getName();

	/**
	 * @return a quests description plus it's status
	 */
	public String getInfo(int questNumber, boolean withReward) {
		if (withReward) {
            return MessageFormat.format(
                MessagesConfig.getMessage("quest.format"),
                String.valueOf(questNumber),
                getName(),
                getProgressString()
            ) + getReward().toString() + "\n";
		}

        return MessageFormat.format(
            MessagesConfig.getMessage("quest.format"),
            String.valueOf(questNumber),
            getName(),
            getProgressString()
        );
	}

	public String getProgressString() {
		if (isCompleted()) {
			return MessagesConfig.getMessage("quest.progress.completed");
		}

		return getCount() + "/" + getGoal();
	}

	public String getLeftString() {
		if (isCompleted()) {
			return MessagesConfig.getMessage("quest.progress.completed");
		}

		return MessageFormat.format(MessagesConfig.getMessage("quest.progress.remaining"), getGoal() - getCount());
	}

	public abstract String[] getOptionNames();

	public abstract String getOptionKey();

	public abstract QuestType getQuestType();

	public boolean isCompleted() {
		return count >= goal;
	}

	public int getGoal() {
		return goal;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Reward getReward() {
		return reward;
	}

	public boolean isRewardReceived() {
		return rewardReceived;
	}

	public void setRewardReceived(boolean rewardReceived) {
		this.rewardReceived = rewardReceived;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
