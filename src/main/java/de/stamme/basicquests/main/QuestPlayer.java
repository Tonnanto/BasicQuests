package de.stamme.basicquests.main;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import de.stamme.basicquests.quest_generation.QuestGenerationException;
import de.stamme.basicquests.quest_generation.QuestGenerator;
import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.quests.QuestData;
import net.md_5.bungee.api.ChatColor;

public class QuestPlayer {

	public final Player player;
	
	public ArrayList<Quest> quests;
	int skipCount;
	
	// new Player on the Server
	public QuestPlayer(Player player) {
		this.player = player;
		resetQuests();
	}
	
	
	public QuestPlayer(PlayerData data, Player player) {
		this.player = player;
		this.skipCount = data.skipCount;
		ArrayList<Quest> quest_arr = new ArrayList<Quest>();
		
		for (QuestData qdata: data.questSnapshot) {
			Quest quest = qdata.toQuest();
			if (quest != null) {
				quest_arr.add(quest);
			}
		}
		this.quests = quest_arr;
		
		refreshQuests();
	}
	
	// resets all of a players quests
	public void resetQuests() {
		this.quests = new ArrayList<Quest>();
		addNewQuests(Config.getQuestAmount());
		QuestsScoreBoardManager.refresh(this);
	}
	
	// fills up missing quests
	private void refreshQuests() {
		int questAmount = Config.getQuestAmount();
		if (quests == null) {
			resetQuests();
		} else if (quests.size() < questAmount) {
			int missing = quests.size() - questAmount;
			addNewQuests(missing);
			QuestsScoreBoardManager.refresh(this);
		}
	}
	
	// adds <amount> quests to players quests
	private void addNewQuests(int amount) {
		
		for (int i = 0; i < amount; i++) {
			try {
				quests.add(QuestGenerator.generate(this));
			} catch (QuestGenerationException e) {
				Main.log(e.message);
				e.printStackTrace();
			}
		}
	}
	
	// removes completed quests and adds new quests after reward has been collected - notifies player
	public void recieveNewQuests() {
		ArrayList<Quest> questsToRemove = new ArrayList<Quest>();
		
		for (Quest q: quests) {
			if (q.rewardRecieved) {
				questsToRemove.add(q);
			}
		}
		
		quests.removeAll(questsToRemove);
		
		int missing = Config.getQuestAmount() - quests.size();
		if (missing > 0) {
			addNewQuests(missing);
			sendMessage(String.format("%sYou recieved %s new quest%s!", ChatColor.AQUA, (missing > 1) ? missing : "a", (missing > 1) ? "s" : ""));
		}
		
		QuestsScoreBoardManager.refresh(this);
	}
	
	// skips a quest at a certain index
	public void skipQuest(int index) {
		
		if (quests != null && quests.size() > index && index >= 0) {
			
			int skipsLeft = Config.getSkipsPerDay() - skipCount;

			if (skipsLeft <= 0 && !player.hasPermission("quests.skip")) {
				sendMessage(String.format("%sYou have no skips left. - Reset in %s", ChatColor.RED, StringFormatter.timeToMidnight()));
				return;
			}
			
			try {
				quests.remove(index);
				quests.add(index, QuestGenerator.generate(this));
				if (!player.hasPermission("quests.skip")) { 
					skipCount++;
					sendMessage(String.format("%sYour %s. quest has been skipped. %s-%s %s skips left for today.", ChatColor.GREEN, index + 1, ChatColor.WHITE, (skipsLeft-1 > 0) ? ChatColor.GREEN : ChatColor.RED, skipsLeft-1));
				} else {
					sendMessage(String.format("%sYour %s. quest has been skipped.", ChatColor.GREEN, index + 1));
				}
				QuestsScoreBoardManager.refresh(this);
				
			} catch (QuestGenerationException e) {
				Main.log(e.message);
				e.printStackTrace();
			}
			
		} else
			sendMessage(String.format("%sNo quest found at index %s.", ChatColor.RED, index + 1));
		
		return;
	}
	
	// completes a quest at a certain index
	public void completeQuest(int index) {
		
		if (quests != null && quests.size() > index && index >= 0) {
			
			Quest quest = quests.get(index);
			if (!quest.completed()) {
				quest.progress(quest.goal, this);
				sendMessage(String.format("%sYour %s. quest has been completed.", ChatColor.GREEN, index + 1));
			} else
				sendMessage(String.format("%sThis quest is already completed.", ChatColor.RED));
			
		} else
			sendMessage(String.format("%sNo quest found at index %s.", ChatColor.RED, index + 1));
		
		return;
	}
	


	// Getter
	public String getName() { return player.getName(); }
	
	// Setter
	public void setSkipCount(int x) { skipCount = x; }
	
	// Convenience methods from bukkit.Player
	public void sendMessage(String message) {
		player.sendMessage(message);
	}
	
	public boolean hasPermission(String key) {
		return player.hasPermission(key);
	}
}
