package de.stamme.basicquests.commands;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.UUID;

public class CompleteQuestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

		// Check for permission
		if (!sender.hasPermission("quests.complete")) {
			sender.sendMessage(ChatColor.RED + Main.l10n("permissions.commandNotAllowed"));
			return true;
		}

		// Popping last two arguments if the command was executed through a ClickEvent in the chat
		int argsLen = args.length;
		boolean clicked = false;
		String clickedQuestID = "";
		if (args.length > 1 && args[args.length-2].equalsIgnoreCase("CLICKED")) {
			clicked = true;
			clickedQuestID = args[args.length-1];
			argsLen -= 2;
		}

		if (!(sender instanceof Player)) {
			// Command executed by console
			return onConsoleCompleteQuest(sender, args);
		}

		// Command executed by player
		Player player = (Player) sender;
		@Nullable QuestPlayer questPlayer = Main.getPlugin().getQuestPlayers().get(player.getUniqueId());//;.getQuestPlayer(player);

		if (argsLen == 0) {
			// Player -> /completequest

			if (questPlayer == null)
				return false;

			// Prompt to select own quest in chat
			promptCompleteSelection(player, questPlayer, null);
			return true;
		}

		if (argsLen == 1) {
			// Check argument
			try {
				int index = Integer.parseInt(args[0]) - 1;
				if (questPlayer == null)
					return false;

				// Player completing his own quest by index
				// QuestPlayer -> /completequest [index]
				return onCompleteQuestByIndex(sender, questPlayer, index, clicked, clickedQuestID);

			} catch (NumberFormatException ignored) {
				// Player completing others quest
				// Player -> /completequest <Player>

				String targetName = args[0];
				return onCompleteQuestForOther(sender, targetName, clicked, clickedQuestID, null);
			}
		}

		if (argsLen == 2) {
			// Player -> /completequest <player> [index]

			// Check arguments
			int questIndex;
			try {
				questIndex = Integer.parseInt(args[1]) - 1;
			} catch (NumberFormatException ignored) {
				return false;
			}

			String targetName = args[0];
			return onCompleteQuestForOther(sender, targetName, clicked, clickedQuestID, questIndex);

		}

		return false;
	}

	/**
	 * Called when the /completequest command has not been executed by player via chat.
	 * @param sender the CommandSender who executed the command
	 * @param args the arguments of the command
	 * @return whether the command was used correctly
	 */
	private boolean onConsoleCompleteQuest(CommandSender sender, String[] args) {
		if (args.length != 2) {
			sender.sendMessage(MessageFormat.format(Main.l10n("commands.usage"), "completequest [player] [index]"));
			return true;
		}

		// Console -> /completequest <player> [index]

		// check permission
		if (!sender.hasPermission("quests.complete.forothers")) {
			sender.sendMessage(ChatColor.RED + Main.l10n("permissions.actionNotAllowed"));
			return true;
		}

		// Check arguments
		int index;
		try {
			index = Integer.parseInt(args[1]) - 1;
		} catch (NumberFormatException ignored) {
			sender.sendMessage(MessageFormat.format(Main.l10n("commands.usage"), "completequest [player] [index]"));
			return true;
		}

		// Look for target QuestPlayer
		String targetName = args[0];
		@Nullable QuestPlayer targetPlayer = findTargetPlayer(sender, targetName);
		if (targetPlayer == null) return true;

		targetPlayer.completeQuest(index, sender);
		return true;
	}

	/** // TODO
	 * Called when a CommandSender tries to complete a QuestPlayers quest by it's index
	 * sender and target can be the same player!
	 * sender -> /completequest <target> [questIndex]
	 * @param sender the CommandSender who executed the command
	 * @param target the QuestPlayer who's quest should be completed
	 * @param questIndex the index of the quest that should be completed
	 * @param clicked whether the sender has clicked on the chat to complete the quest
	 * @param clickedQuestID the ID of the clicked quest.
	 * @return whether the command was used correctly
	 */
	private boolean onCompleteQuestByIndex(CommandSender sender, QuestPlayer target, int questIndex, boolean clicked, @Nullable String clickedQuestID) {
		// Check if the clicked quest is the quest at the given index
		if (target.getQuests().size() > questIndex) {
			String questID = target.getQuests().get(questIndex).getId();
			if (clicked && (questID == null || !questID.equals(clickedQuestID))) {
				sender.sendMessage(ChatColor.RED + Main.l10n("commands.questAlreadyCompleted"));
				return true;
			}
		}

		target.completeQuest(questIndex, sender);
		return true;
	}


	/**
	 * Called when a CommandSender tries to complete a players quest
	 * sender -> /completequest <target> [questIndex]
	 * sender -> /completequest <target>
	 * @param sender the CommandSender who executed the command
	 * @param targetName the player who's quest should be completed
	 * @param clicked whether the sender has clicked on the chat to complete the quest
	 * @param clickedQuestID the ID of the clicked quest.
	 * @param questIndex the index of the quest that should be completed
	 * @return whether the command was used correctly
	 */
	private boolean onCompleteQuestForOther(CommandSender sender, String targetName, boolean clicked, @Nullable String clickedQuestID, @Nullable Integer questIndex) {
		// check permission
		if (!sender.hasPermission("quests.complete.forothers")) {
			sender.sendMessage(ChatColor.RED + Main.l10n("permissions.actionNotAllowed"));
			return true;
		}

		// Find the targeted quest player
		QuestPlayer targetPlayer = findTargetPlayer(sender, targetName);
		if (targetPlayer == null)
			return true;

		if (questIndex != null) {
			// Select other players quest by index
			return onCompleteQuestByIndex(sender, targetPlayer, questIndex, clicked, clickedQuestID);
		}

		if (sender instanceof Player) {
			// Select other players quest in chat
			promptCompleteSelection((Player) sender, targetPlayer, targetName);
		}
		return true;
	}


	/**
	 * Finds a QuestPlayer based on the given name
	 * @param sender the CommandSender who executed the command
	 * @param targetName the name of the targeted player
	 * @return the found QuestPlayer or null
	 */
	@Nullable
	private QuestPlayer findTargetPlayer(CommandSender sender, String targetName) {

		// Check if targeted player is online
		Player target = Main.getPlugin().getServer().getPlayer(targetName);
		if (target == null) {
			sender.sendMessage(ChatColor.RED + MessageFormat.format(Main.l10n("commands.playerNotFound"), targetName));
			return null;
		}

		// Check if targeted player is QuestPlayer
		QuestPlayer targetPlayer = Main.getPlugin().getQuestPlayer(target);
		if (targetPlayer == null) {
			sender.sendMessage(ChatColor.RED + Main.l10n("commands.questPlayerNotFound"));
			return null;
		}

		return targetPlayer;
	}

	/**
	 * Shows a list of all possible quests to skip for the given player.
	 * Prompts the sender to select a quest by clicking it in the chat.
	 * A ClickEvent will be fired if a quest is clicked.
	 * This event will execute another /completequest command with the index.
	 * @param selector the player to be prompted
	 * @param target the players who's quest should be skipped
	 * @param targetNameArgument the targets name to put in the new command. Null if selector and target are the same player.
	 */
	public void promptCompleteSelection(Player selector, QuestPlayer target, @Nullable String targetNameArgument) {

		if (selector == target.getPlayer()) {
			selector.sendMessage(ChatColor.AQUA + "\n" + Main.l10n("commands.clickQuestToComplete"));
		} else {
			selector.sendMessage(ChatColor.AQUA + "\n" + MessageFormat.format(Main.l10n("commands.clickQuestToCompleteForOther"), target.getName()));
		}

		StringBuilder command = new StringBuilder("/completequest");
		if (targetNameArgument != null) {
			command.append(" ").append(targetNameArgument);
		}

		for (int i = 0; i < target.getQuests().size(); i++) {
			Quest quest = target.getQuests().get(i);
			if (quest.getId() == null)
				quest.setId(UUID.randomUUID().toString());

			TextComponent questText = new TextComponent(String.format(" %s> %s%s", ChatColor.AQUA, ChatColor.UNDERLINE, quest.getInfo(false)));
			questText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Main.l10n("commands.clickToCompleteTooltip"))));
			questText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (i+1) + " CLICKED " + quest.getId()));

			selector.spigot().sendMessage(questText);
		}
	}
}
