package de.stamme.basicquests.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Lists a player's current quests in the chat
public class QuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) return false;

		QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer((Player) sender);
		if (questPlayer == null) {
			sender.sendMessage(ChatColor.RED + "No Quests found!");
			return true;
		} // is QuestPlayer

		if (questPlayer.getQuests().size() <= 0) {
			questPlayer.sendMessage(ChatColor.RED + "No Quests found!");
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
		ComponentBuilder message = new ComponentBuilder("\nYour Quests:  ");
		TextComponent showRewardsButton = new TextComponent(String.format("%s>> Show Rewards <<", ChatColor.AQUA));
		showRewardsButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to show rewards.")));
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
		StringBuilder message = new StringBuilder("\nYour Quests and Rewards:");
		for (int i = 0; i < questPlayer.getQuests().size(); i++) {
			Quest q = questPlayer.getQuests().get(i);
			if (i != 0)
				message.append("\n ");

			message.append(String.format("\n %s>%s %s", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(true)));
		}
		questPlayer.sendMessage(message.toString());
	}
}
