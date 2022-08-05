package de.stamme.basicquests.commands;

import de.stamme.basicquests.util.L10n;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

// Lists a player's current quests in the chat
public class QuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) return false;

		QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer((Player) sender);
		if (questPlayer == null) {
			sender.sendMessage(ChatColor.RED + L10n.getMessage("quests.noQuestsFound"));
			return true;
		} // is QuestPlayer

		if (questPlayer.getQuests().size() <= 0) {
			sender.sendMessage(ChatColor.RED + L10n.getMessage("quests.noQuestsFound"));
			return true;
		} // QuestPlayer has Quests

		// "/quests"
		if (args == null || args.length == 0) {
			sendQuestsMessage(questPlayer);
			return true;
		}

		// "/quests detail"
		if (args.length == 1 && args[0].equals("detail")) {
			sendQuestDetailMessage(questPlayer);
			return true;
		}
		return false;
	}

	String buildBasicQuestInfoMessage(Quest quest) {
		return String.format(" %s>%s %s", ChatColor.GOLD, ChatColor.WHITE, quest.getInfo(false));
	}

	/**
	 * sends a message containing a list of all active quests.
	 * also shows a button that triggers /quests detail
	 * @param questPlayer the player to send this message to
	 */
	void sendQuestsMessage(QuestPlayer questPlayer) {
		ComponentBuilder message = new ComponentBuilder("\n" + L10n.getMessage("quests.yourQuests") + ":  ");
		TextComponent showRewardsButton = new TextComponent(ChatColor.AQUA + ">> " + L10n.getMessage("rewards.showRewards") + " <<");
		showRewardsButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(L10n.getMessage("rewards.clickToShowRewardsTooltip"))));
		showRewardsButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests detail"));
		message.append(showRewardsButton);
		questPlayer.getPlayer().spigot().sendMessage(message.create());

		for (Quest quest: questPlayer.getQuests()) {
			questPlayer.sendMessage(buildBasicQuestInfoMessage(quest));
		}
	}

	/**
	 * sends a message containing a list of all active quests as well as their rewards.
	 * @param questPlayer the player to send this message to
	 */
	void sendQuestDetailMessage(QuestPlayer questPlayer) {
		StringBuilder message = new StringBuilder("\n" + L10n.getMessage("quests.questsAndRewards") + ":");
		for (int i = 0; i < questPlayer.getQuests().size(); i++) {
			Quest q = questPlayer.getQuests().get(i);
			if (i != 0)
				message.append("\n ");

			message.append(String.format("\n %s>%s %s", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(true)));
		}
		questPlayer.sendMessage(message.toString());
	}
}
