package de.stamme.basicquests.model;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.ServerInfo;
import de.stamme.basicquests.model.generation.QuestGenerationException;
import de.stamme.basicquests.questgeneration.QuestGenerator;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.quests.QuestData;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.util.fastboard.QuestsScoreBoardManager;
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
				BasicQuestsPlugin.log(e.message);
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

        BasicQuestsPlugin.log(Integer.toString(Config.getQuestAmount()));

		int missing = Config.getQuestAmount() - quests.size();
		if (missing > 0) {
			addNewQuests(missing, true);
		}

		QuestsScoreBoardManager.refresh(this);
	}

	/**
	 * skips a quest at a certain index
	 * @param index index of quest to skip
	 * @param sender the player who initiated the skip
	 */
	public void skipQuest(int index, CommandSender sender) {
		if (getQuests() == null || getQuests().size() <= index || index < 0) {
			BasicQuestsPlugin.sendMessage(sender, MessageFormat.format(MessagesConfig.getMessage("commands.skip.not-found"), index + 1));
			return;
		}

		int skipsLeft = Config.getSkipsPerDay() - getSkipCount();

		if (sender == getPlayer() && skipsLeft <= 0 && !hasPermission("basicquests.skip.unlimited")) {
			sendMessage(MessageFormat.format(MessagesConfig.getMessage("commands.skip.none"), StringFormatter.timeToMidnight()));
			return;
		}

		try {
			if (!hasPermission("basicquests.skip.unlimited")) {
				if (sender == getPlayer())
					increaseSkipCount();

				String message = MessageFormat.format(MessagesConfig.getMessage("commands.skip.skipped"), index + 1);

				message += ChatColor.WHITE + " - " + ((getSkipsLeft() > 0) ? ChatColor.GREEN : ChatColor.RED);

				ChoiceFormat skipsFormat = new ChoiceFormat(new double[]{0, 1, 2}, new String[]{
                    MessagesConfig.getMessage("skip.none"),
                    MessagesConfig.getMessage("skip.singular"),
                    MessagesConfig.getMessage("skip.plural"),
				});

				message += MessageFormat.format(MessagesConfig.getMessage("commands.skip.remaining"), getSkipsLeft(), skipsFormat.format(getSkipsLeft()));

				sendMessage(message);
			} else {
                sendMessage(MessageFormat.format(MessagesConfig.getMessage("commands.skip.skipped"), index + 1));
            }

			if (sender != getPlayer())
				BasicQuestsPlugin.sendMessage(sender, MessageFormat.format(MessagesConfig.getMessage("commands.skip.skipped-other"), getPlayer().getName(), index + 1));

			// Remove Quest and add it to ServerInfo.skippedQuests
			Quest skippedQuest = getQuests().remove(index);
			if (!hasPermission("basicquests.skip.unlimited")) // Do not include skips of players with unlimited skips
				ServerInfo.getInstance().questSkipped(skippedQuest);

			// Generate new Quest
			Quest newQuest = QuestGenerator.getInstance().generate(this);
			getQuests().add(index, newQuest);
			announceQuests(newQuest);
			QuestsScoreBoardManager.refresh(this);

		} catch (QuestGenerationException e) {
			BasicQuestsPlugin.log(e.message);
			e.printStackTrace();
		}
	}

	/**
	 * completes a quest at a certain index
	 * @param index index of quest to complete
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

        quest.progress(quest.getGoal(), this);

        if (sender == getPlayer()) {
            sendMessage(MessageFormat.format(MessagesConfig.getMessage("commands.complete.success"), index + 1));
            return;
        }

        BasicQuestsPlugin.sendMessage(
            sender,
            MessageFormat.format(MessagesConfig.getMessage("commands.complete.success-other"), getPlayer().getName(), index + 1)
        );
	}

	/**
	 * sends a message to the player announcing the given quests.
	 * @param quests quests to announce.
	 */
	private void announceQuests(Quest... quests) {
		if (quests.length <= 0) {
            return;
        }

		StringBuilder sb = new StringBuilder();

		ChoiceFormat questsFormat = new ChoiceFormat(new double[]{1, 2}, new String[]{
            MessagesConfig.getMessage("generic.quest.singular"),
            MessagesConfig.getMessage("generic.quest.plural"),
		});

        // TODO
		for (Quest q: quests) {
			sb.append(String.format("> %s\n", q.getInfo(true)));
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(BasicQuestsPlugin.getPlugin(), () -> {
            BasicQuestsPlugin.sendMessage(player, MessageFormat.format(MessagesConfig.getMessage("events.player.new-quest"), questsFormat.format(quests.length)));
            player.sendMessage(sb.toString());
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
			Quest q = getQuests().get(i);
			if (i != 0) message.append("\n");
			message.append(String.format("> %s", q.getInfo(false)));
		}
		return message.toString();
	}

	public String getQuestsWithRewardsMessage() {
		StringBuilder message = new StringBuilder();
		for (int i = 0; i < getQuests().size(); i++) {
			Quest q = getQuests().get(i);
			if (i != 0) message.append("\n");
			message.append(String.format("\n> %s", q.getInfo(true)));
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

	public int getShowScoreboard() {
		return showScoreboard;
	}

	public void setShowScoreboard(int showScoreboard) {
		this.showScoreboard = Math.min(showScoreboard, 2);
	}
}
