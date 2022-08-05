package de.stamme.basicquests.model;

import de.stamme.basicquests.Config;
import de.stamme.basicquests.ServerInfo;
import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.generation.QuestGenerationException;
import de.stamme.basicquests.questgeneration.QuestGenerator;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.quests.QuestData;
import de.stamme.basicquests.util.L10n;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import de.stamme.basicquests.util.StringFormatter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The representation of a player in Basic Quests
 * This player has a list of quests and a certain number of skips per day
 */
public class QuestPlayer {


	// ---------------------------------------------------------------------------------------
	// Player State
	// ---------------------------------------------------------------------------------------

	private final Player player;
	private transient Inventory rewardInventory;
	
	private List<Quest> quests;
	private int skipCount;


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
		this.skipCount = data.skipCount;
		List<Quest> quest_arr = new ArrayList<>();
		
		for (QuestData questData: data.questSnapshot) {
			// Skip invalid quests so they get regenerated
			if (questData.isInvalid()) continue;

			Quest quest = questData.toQuest();
			if (quest != null) {
				quest_arr.add(quest);
			}
		}
		this.quests = quest_arr;
		
		refreshQuests();
	}



	// ---------------------------------------------------------------------------------------
	// Functionality
	// ---------------------------------------------------------------------------------------

	/**
	 * resets all of a players quests
	 */
	public void resetQuests() {
		this.quests = new ArrayList<>();
		addNewQuests(Config.getQuestAmount(), false);
		QuestsScoreBoardManager.refresh(this);
	}

	/**
	 * fills up missing quests
 	 */
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

	/**
	 * adds <amount> quests to players quests
	 * @param amount number of quests to add to player
	 * @param announce whether to send a message to the player announcing the new quest
	 */
	private void addNewQuests(int amount, boolean announce) {
		if (amount < 0) return;
		Quest[] questsToAnnounce = new Quest[amount];
		for (int i = 0; i < amount; i++) {
			try {
				Quest quest = QuestGenerator.getInstance().generate(this);
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

	/**
	 * removes completed quests and adds new quests after reward has been collected - notifies player
	 */
	public void receiveNewQuests() {
		List<Quest> questsToRemove = new ArrayList<>();
		
		for (Quest q: quests) {
			if (q.isRewardReceived()) {
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

	/**
	 * skips a quest at a certain index
	 * @param index index of quest to skip
	 * @param initiator the player who initiated the skip
	 */
	public void skipQuest(int index, CommandSender initiator) {

		if (getQuests() == null || getQuests().size() <= index || index < 0) {
			initiator.sendMessage(ChatColor.RED + MessageFormat.format(L10n.getMessage("player.questAtIndexNotFound"), index + 1));
			return;
		}

		int skipsLeft = Config.getSkipsPerDay() - getSkipCount();

		if (initiator == getPlayer() && skipsLeft <= 0 && !hasPermission("quests.skip")) {
			sendMessage(ChatColor.RED + MessageFormat.format(L10n.getMessage("player.noSkipsLeftInfo"), StringFormatter.timeToMidnight()));
			return;
		}

		try {

			if (!hasPermission("quests.skip")) {
				if (initiator == getPlayer())
					increaseSkipCount();
				String message = ChatColor.GREEN + MessageFormat.format(L10n.getMessage("player.questAtIndexSkipped"), index + 1);
				message += ChatColor.WHITE + " - " + ((getSkipsLeft() > 0) ? ChatColor.GREEN : ChatColor.RED);
				ChoiceFormat skipsFormat = new ChoiceFormat(new double[]{0, 1, 2}, new String[]{
						L10n.getMessage("skip.none"),
						L10n.getMessage("skip.singular"),
						L10n.getMessage("skip.plural"),
				});
				message += MessageFormat.format(L10n.getMessage("player.skipsLeftInfo"), getSkipsLeft(), skipsFormat.format(getSkipsLeft()));
				sendMessage(message);
			} else
				sendMessage(ChatColor.GREEN + MessageFormat.format(L10n.getMessage("player.questAtIndexSkipped"), index + 1));

			if (initiator != getPlayer())
				initiator.sendMessage(ChatColor.GREEN + MessageFormat.format(L10n.getMessage("player.otherPlayersQuestAtIndexSkipped"), getPlayer().getName(), index + 1));

			// Remove Quest and add it to ServerInfo.skippedQuests
			Quest skippedQuest = getQuests().remove(index);
			if (!hasPermission("quests.skip")) // Do not include skips of players with unlimited skips
				ServerInfo.getInstance().questSkipped(skippedQuest);

			// Generate new Quest
			Quest newQuest = QuestGenerator.getInstance().generate(this);
			getQuests().add(index, newQuest);
			announceQuests(newQuest);
			QuestsScoreBoardManager.refresh(this);

		} catch (QuestGenerationException e) {
			Main.log(e.message);
			e.printStackTrace();
		}
	}

	/**
	 * completes a quest at a certain index
	 * @param index index of quest to complete
	 * @param initiator the player who initiated the completion
	 */
	public void completeQuest(int index, CommandSender initiator) {
		if (getQuests() == null || getQuests().size() <= index || index < 0) {
			initiator.sendMessage(ChatColor.RED + MessageFormat.format(L10n.getMessage("player.questAtIndexNotFound"), index + 1));
			return;
		}

		Quest quest = getQuests().get(index);
		if (quest.isCompleted()) {
			initiator.sendMessage(ChatColor.RED + L10n.getMessage("commands.questAlreadyCompleted"));
			return;
		}

		quest.progress(quest.getGoal(), this);
		sendMessage(ChatColor.GREEN + MessageFormat.format(L10n.getMessage("player.questAtIndexCompleted"), index + 1));

		if (initiator != getPlayer())
			initiator.sendMessage(ChatColor.GREEN + MessageFormat.format(L10n.getMessage("player.otherPlayersQuestAtIndexCompleted"), getPlayer().getName(), index + 1));
	}

	/**
	 * sends a message to the player announcing the given quests.
	 * @param quests quests to announce.
	 */
	private void announceQuests(Quest... quests) {
		if (quests.length <= 0) return;

		// build localized message
		StringBuilder sb = new StringBuilder();

		ChoiceFormat questsFormat = new ChoiceFormat(new double[]{1, 2}, new String[]{
				L10n.getMessage("quest.singular"),
				L10n.getMessage("quest.plural"),
		});
		String questString = MessageFormat.format(L10n.getMessage("player.newQuestReceived"), questsFormat.format(quests.length));
		sb.append(ChatColor.AQUA).append(ChatColor.BOLD).append("\n").append(questString).append(":\n");

		for (Quest q: quests) {
			sb.append(String.format(" %s>%s %s\n", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(true)));
		}

		// Send message with a delay
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> player.sendMessage(sb.toString()), 60L);
	}

	// Convenience methods from bukkit.Player
	public void sendMessage(String message) {
		player.sendMessage(message);
	}



	// ---------------------------------------------------------------------------------------
	// Getter & Setter
	// ---------------------------------------------------------------------------------------

	public String getQuestsMessage() {
		StringBuilder message = new StringBuilder();
		for (int i = 0; i < getQuests().size(); i++) {
			Quest q = getQuests().get(i);
			if (i != 0) message.append("\n");
			message.append(String.format(" %s>%s %s", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(false)));
		}
		return message.toString();
	}

	public String getQuestsWithRewardsMessage() {
		StringBuilder message = new StringBuilder();
		for (int i = 0; i < getQuests().size(); i++) {
			Quest q = getQuests().get(i);
			if (i != 0) message.append("\n ");
			message.append(String.format("\n %s>%s %s", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(true)));
		}
		return message.toString();
	}

	public String getName() {
		return player.getName();
	}

	public int getSkipCount() {
		return skipCount;
	}

	public int getSkipsLeft() {
		return Config.getSkipsPerDay() - skipCount;
	}

	public void setSkipCount(int x) {
		skipCount = x;
	}

	public void increaseSkipCount() {
		skipCount++;
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

	public boolean isScoreboardShowing() {
		return getPlayer().getScoreboard().getObjectives().size() > 0;
	}
}
