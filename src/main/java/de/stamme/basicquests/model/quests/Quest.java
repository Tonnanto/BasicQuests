package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.ServerInfo;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.util.fastboard.QuestsScoreBoardManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;

import java.text.MessageFormat;

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
		count = Math.min(count + x, goal);

		// Notify player about progress
		if (x >= 0) { // don't notify if progress is negative
			if (Config.limitProgressMessages()) {
				// Only notify on 25%, 50%, 75% and 100%
				double currentProgress = (double) getCount() / getGoal();
				double prevProgress = (double) (getCount() - 1) / getGoal();

				for (int i = 100; i > 0; i -= 25) {
					boolean quarterAchieved = currentProgress >= (double) i / 100 && prevProgress < (double) i / 100;

					if (quarterAchieved) {
						questPlayer.sendMessage(i + "% " + String.format(MessagesConfig.getMessage("quests.questProgress") + " %s>%s ", ChatColor.GOLD, ChatColor.WHITE) + getInfo(false));
						break;
					}
				}

			} else {
				// Always notify
				questPlayer.sendMessage(String.format(MessagesConfig.getMessage("quests.questProgress") + " %s>%s ", ChatColor.GOLD, ChatColor.WHITE) + getInfo(false));
			}
		}

		// Show title if Quest is completed
		if (isCompleted()) {
			if (Config.broadcastOnQuestCompletion())
				broadcastOnCompletion(questPlayer);

			TextComponent message = new TextComponent("\n        >> " + ChatColor.UNDERLINE + MessagesConfig.getMessage("quests.collectReward") + ChatColor.RESET + " <<\n");
			message.setColor(ChatColor.AQUA);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests reward"));
			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MessagesConfig.getMessage("quests.clickToCollectRewardTooltip"))));

			questPlayer.getPlayer().spigot().sendMessage(message);
			questPlayer.sendMessage(ChatColor.GRAY + MessageFormat.format(MessagesConfig.getMessage("quests.receiveRewardInfo"), "/quests reward"));
			questPlayer.getPlayer().sendTitle(ChatColor.GREEN + MessagesConfig.getMessage("quests.questCompleted"), getName(), 10, 70, 20);

			if (Config.soundOnQuestCompletion()) {
				// Play Sound
				Location playerLocation = questPlayer.getPlayer().getLocation();
				questPlayer.getPlayer().playSound(playerLocation, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 10);
			}

			ServerInfo.getInstance().questCompleted(this); // Add completed Quest to ServerInfo.completedQuests
		}

		QuestsScoreBoardManager.refresh(questPlayer);
	}

	private void broadcastOnCompletion(QuestPlayer questPlayer) {
		String broadcastMessage = ChatColor.YELLOW + MessageFormat.format(MessagesConfig.getMessage("quests.completionBroadcast"), questPlayer.getPlayer().getName());
		broadcastMessage += " > " + getName();
		Bukkit.getServer().broadcastMessage(broadcastMessage);
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
	public String getInfo(boolean withReward) {
		if (withReward) {
			return ChatColor.YELLOW + getName() + " " + ChatColor.GREEN + "(" + getProgressString() + ")\n  " +
					ChatColor.WHITE + ChatColor.ITALIC + ChatColor.UNDERLINE +
					MessagesConfig.getMessage("quests.reward") + ":" +
					ChatColor.RESET + ChatColor.ITALIC + " " + getReward().toString();
		} else {
			return ChatColor.YELLOW + getName() + " " + ChatColor.GREEN + "(" + getProgressString() + ")";
		}
	}

	public String getProgressString() {
		if (isCompleted()) {
			return MessagesConfig.getMessage("quests.completed");
		}
		return count + "/" + goal;
	}

	public String getLeftString() {
		if (isCompleted()) {
			return MessagesConfig.getMessage("quests.completed");
		}
		return MessageFormat.format(MessagesConfig.getMessage("quests.left"), goal - count);
	}

	public abstract String[] getDecisionObjectNames();

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
