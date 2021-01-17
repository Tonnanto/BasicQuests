package de.stamme.basicquests.main;

import de.stamme.basicquests.data.Config;
import de.stamme.basicquests.data.ServerInfo;
import de.stamme.basicquests.quest_generation.QuestGenerationException;
import de.stamme.basicquests.quest_generation.QuestGenerator;
import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.quests.QuestData;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import de.stamme.basicquests.util.StringFormatter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
import java.util.ArrayList;

public class QuestPlayer {

	public final Player player;
	public transient Inventory rewardInventory;
	
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
		ArrayList<Quest> quest_arr = new ArrayList<>();
		
		for (QuestData qdata: data.questSnapshot) {
			if (qdata.getReward().money.compareTo(BigDecimal.ZERO) > 0 && !Config.moneyRewards()) continue;
			if (qdata.getReward().xp > 0 && !Config.xpRewards()) continue;
			if (qdata.getReward().items.size() > 0 && !Config.itemRewards()) continue;
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
		this.quests = new ArrayList<>();
		addNewQuests(Config.getQuestAmount(), false);
		QuestsScoreBoardManager.refresh(this);
	}
	
	// fills up missing quests
	private void refreshQuests() {
		int questAmount = Config.getQuestAmount();
		if (quests == null) {
			resetQuests();
		} else if (quests.size() < questAmount) {
			int missing = questAmount - quests.size();
			addNewQuests(missing, false);
			QuestsScoreBoardManager.refresh(this);
		}
	}
	
	// adds <amount> quests to players quests
	private void addNewQuests(int amount, boolean announce) {
		if (amount < 0) return;
		Quest[] questsToAnnounce = new Quest[amount];
		for (int i = 0; i < amount; i++) {
			try {
				Quest quest = QuestGenerator.generate(this);
				quests.add(quest);
				questsToAnnounce[i] = quest;
			} catch (QuestGenerationException e) {
				Main.log(e.message);
				e.printStackTrace();
			}
		}
		if (announce)
			announceQuests(questsToAnnounce);
	}
	
	// removes completed quests and adds new quests after reward has been collected - notifies player
	public void receiveNewQuests() {
		ArrayList<Quest> questsToRemove = new ArrayList<>();
		
		for (Quest q: quests) {
			if (q.rewardReceived) {
				questsToRemove.add(q);
			}
		}
		
		quests.removeAll(questsToRemove);
		
		int missing = Config.getQuestAmount() - quests.size();
		if (missing > 0) {
			addNewQuests(missing, true);
		}
		
		QuestsScoreBoardManager.refresh(this);
	}
	
	// skips a quest at a certain index
	public void skipQuest(int index, CommandSender initiator) {
		
		if (quests != null && quests.size() > index && index >= 0) {
			
			int skipsLeft = Config.getSkipsPerDay() - skipCount;

			if (initiator == this.player && skipsLeft <= 0 && !hasPermission("quests.skip")) {
				sendMessage(String.format("%sYou have no skips left. - Reset in %s", ChatColor.RED, StringFormatter.timeToMidnight()));
				return;
			}
			
			try {

				if (!hasPermission("quests.skip")) {
					if (initiator == this.player)
						skipCount++;
					sendMessage(String.format("%sYour %s. quest has been skipped. %s-%s %s skip%s left for today.", ChatColor.GREEN, index + 1, ChatColor.WHITE, (getSkipsLeft() > 0) ? ChatColor.GREEN : ChatColor.RED, getSkipsLeft(), (getSkipsLeft() == 1) ? "" : "s"));
				} else {
					sendMessage(String.format("%sYour %s. quest has been skipped.", ChatColor.GREEN, index + 1));
				}
				if (initiator != this.player)
					initiator.sendMessage(String.format("%s%s's %s. quest has been skipped.", ChatColor.GREEN, this.player.getName(), index + 1));

				Quest newQuest = QuestGenerator.generate(this);
				ServerInfo.getInstance().questSkipped(quests.remove(index)); // Remove Quest and add it to ServerInfo.skippedQuests
				quests.add(index, newQuest);
				announceQuests(newQuest);
				QuestsScoreBoardManager.refresh(this);

			} catch (QuestGenerationException e) {
				Main.log(e.message);
				e.printStackTrace();
			}
			
		} else {
			initiator.sendMessage(String.format("%sNo quest found at index %s.", ChatColor.RED, index + 1));
		}

	}
	
	// completes a quest at a certain index
	public void completeQuest(int index, CommandSender initiator) {
		
		if (quests != null && quests.size() > index && index >= 0) {
			
			Quest quest = quests.get(index);
			if (!quest.completed()) {
				quest.progress(quest.goal, this);
				sendMessage(String.format("%sYour %s. quest has been completed.", ChatColor.GREEN, index + 1));

				if (initiator != this.player)
					initiator.sendMessage(String.format("%s%s's %s. quest has been completed.", ChatColor.GREEN, this.player.getName(), index + 1));
			} else
				initiator.sendMessage(String.format("%sThis quest has already been completed.", ChatColor.RED));
		} else
			initiator.sendMessage(String.format("%sNo quest found at index %s.", ChatColor.RED, index + 1));
	}

	private void announceQuests(Quest... quests) {
		if (quests.length <= 0) return;
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s%sNew Quest%s:\n",ChatColor.AQUA, ChatColor.BOLD, (quests.length > 1) ? "s" : ""));
		for (Quest q: quests) {
			sb.append(String.format(" %s>%s %s\n", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(true)));
		}

		// Send message with a delay
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> player.sendMessage(sb.toString()), 60L);
	}

	// Getter
	public String getName() {
		return player.getName();
	}

	public int getSkipCount() {
		return skipCount;
	}

	public int getSkipsLeft() {
		return Config.getSkipsPerDay() - skipCount;
	}
	
	// Setter
	public void setSkipCount(int x) {
		skipCount = x;
	}
	
	// Convenience methods from bukkit.Player
	public void sendMessage(String message) {
		player.sendMessage(message);
	}
	
	public boolean hasPermission(String key) {
		return player.hasPermission(key);
	}
}
