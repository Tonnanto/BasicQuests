package de.stamme.basicquests.quests;

import de.stamme.basicquests.data.Config;
import de.stamme.basicquests.data.ServerInfo;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;

abstract public class Quest {

	// ---------------------------------------------------------------------------------------
	// Quest State
	// ---------------------------------------------------------------------------------------

	private final int goal;
	private final Reward reward;
	private int count = 0;
	private boolean rewardReceived = false;

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
		if (isCompleted()) {

			if (Config.broadcastOnQuestCompletion())
				broadcastOnCompletion(questPlayer);

			TextComponent message = new TextComponent(String.format("\n        >> %sCollect Reward!%s <<\n", ChatColor.UNDERLINE, ChatColor.RESET));
			message.setColor(ChatColor.AQUA);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/getreward"));
			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to receive all pending rewards")));

			questPlayer.getPlayer().spigot().sendMessage(message);
			questPlayer.sendMessage(String.format("%sClick above or use /getreward to receive your Reward!", ChatColor.GRAY));
			questPlayer.getPlayer().sendTitle(String.format("%sQuest Completed!", ChatColor.GREEN), getName(), 10, 70, 20);

			ServerInfo.getInstance().questCompleted(this); // Add completed Quest to ServerInfo.completedQuests
			
		} else if (x >= 0) { // don't notify if progress is negative
			questPlayer.sendMessage(String.format("Quest Progress! %s>%s ", ChatColor.GOLD, ChatColor.WHITE) + getInfo(false));
		}
		
		QuestsScoreBoardManager.refresh(questPlayer);
	}

	private void broadcastOnCompletion(QuestPlayer questPlayer) {
		Bukkit.getServer().broadcastMessage(String.format("%s%s completed a quest! > %s", ChatColor.YELLOW, questPlayer.getPlayer().getName(), getName()));
	}

	/**
	 * Creates a QuestData Object from this Quest
	 * This Object contains this Quests state so it can be serialized and persisted.
	 */
	public QuestData toData() {
		QuestData data = new QuestData();
		
		data.setGoal(goal);
		data.setCount(count);
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
	 * Returns a quests description plus it's status
	 */
	public String getInfo(boolean withReward) {
		if (withReward) {
			if (isCompleted()) {
				return String.format("%s%s %s(Completed!)\n   %s%s%sReward:%s%s %s", ChatColor.YELLOW, getName(), ChatColor.GREEN, ChatColor.WHITE, ChatColor.ITALIC, ChatColor.UNDERLINE, ChatColor.RESET, ChatColor.ITALIC, getReward().toString());
			}
			return String.format("%s%s %s(%s/%s)\n   %s%s%sReward:%s%s %s", ChatColor.YELLOW, getName(), ChatColor.GREEN, count, goal, ChatColor.WHITE, ChatColor.ITALIC, ChatColor.UNDERLINE, ChatColor.RESET, ChatColor.ITALIC, getReward().toString());
		} else {
			if (isCompleted()) {
				return String.format("%s%s %s(Completed!)", ChatColor.YELLOW, getName(), ChatColor.GREEN);
			}
			return String.format("%s%s %s(%s/%s)", ChatColor.YELLOW, getName(), ChatColor.GREEN, count, goal);
		}
	}

	public abstract String[] getDecisionObjectNames();

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
}