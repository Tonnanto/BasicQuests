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

	public int goal;
	public int count = 0;
	public Reward reward;
	public boolean rewardReceived = false;
	
	public Quest(int goal, Reward reward) {
		this.goal = goal;
		this.reward = reward;
	}
	
	// returns a boolean whether the quest is completed
	public boolean completed() {
		return count >= goal;
	}
	
	// adds x to the Quest.count and notifies the player
	public void progress(int x, QuestPlayer player) {
		if (count == goal) { return; }
		count = Math.min(count + x, goal);
		if (completed()) {

			if (Config.broadcastOnQuestCompletion())
				broadcastOnCompletion(player);

			TextComponent message = new TextComponent(">> Collect Reward! <<");
			message.setColor(ChatColor.GOLD);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/getreward"));
			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to receive")));

			player.player.spigot().sendMessage(message);
			player.sendMessage(String.format("%sClick above or use /getreward to receive your Reward!", ChatColor.GRAY));
			player.player.sendTitle(String.format("%sQuest Completed!", ChatColor.GREEN), getName(), 10, 70, 20);

			ServerInfo.getInstance().questCompleted(this); // Add completed Quest to ServerInfo.completedQuests
			
		} else if (x >= 0) { // don't notify if progress is negative
			player.sendMessage(String.format("Quest Progress! %s>%s ", ChatColor.GOLD, ChatColor.WHITE) + getInfo(false));
		}
		
		QuestsScoreBoardManager.refresh(player);
	}
	
	public QuestData toData() {
		QuestData data = new QuestData();
		
		data.goal = goal;
		data.count = count;
		data.reward = reward;
		data.rewardReceived = rewardReceived;
		
		return data;
	}
	
	// returns the description of the quest. 
	public abstract String getName();
	
	// Returns a quests description plus it's status
	public String getInfo(boolean withReward) {
		if (withReward) {
			if (completed()) {
				return String.format("%s%s %s(Completed!)\n   %s%s%sReward:%s%s %s", ChatColor.YELLOW, getName(), ChatColor.GREEN, ChatColor.WHITE, ChatColor.ITALIC, ChatColor.UNDERLINE, ChatColor.RESET, ChatColor.ITALIC, reward.toString());
			}
			return String.format("%s%s %s(%s/%s)\n   %s%s%sReward:%s%s %s", ChatColor.YELLOW, getName(), ChatColor.GREEN, count, goal, ChatColor.WHITE, ChatColor.ITALIC, ChatColor.UNDERLINE, ChatColor.RESET, ChatColor.ITALIC, reward.toString());
		} else {
			if (completed()) {
				return String.format("%s%s %s(Completed!)", ChatColor.YELLOW, getName(), ChatColor.GREEN);
			}
			return String.format("%s%s %s(%s/%s)", ChatColor.YELLOW, getName(), ChatColor.GREEN, count, goal);
		}
		
	}
	
	public abstract String[] getDecisionObjectNames();
	
	private void broadcastOnCompletion(QuestPlayer player) {
		 Bukkit.getServer().broadcastMessage(String.format("%s%s completed a quest! > %s", ChatColor.YELLOW, player.player.getName(), getName()));
	}
}