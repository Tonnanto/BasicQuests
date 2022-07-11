package de.stamme.basicquests.commands;

import de.stamme.basicquests.data.Config;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.util.StringFormatter;
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

import java.util.UUID;

public class SkipQuestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

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
			return onConsoleSkipQuest(sender, args);
		}

		// Command executed by player
		Player player = (Player) sender;
		@Nullable QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer(player);

		if (argsLen == 0) {
			// Player -> /skipquest

			if (questPlayer == null) return false;
			
			// Check skips / permission
			int skipsLeft = Config.getSkipsPerDay() - questPlayer.getSkipCount();
			if (skipsLeft <= 0 && !sender.hasPermission("quests.skip")) {
				questPlayer.sendMessage(String.format("%sYou have no skips left. - Reset in %s", ChatColor.RED, StringFormatter.timeToMidnight()));
				return true;
			}

			// Prompt to select own quest in chat
			promptSkipSelection(player, questPlayer, null);
			return true;
		}

		if (argsLen == 1) {
			// Check argument
			try {
				int index = Integer.parseInt(args[0]) - 1;
				if (questPlayer == null) return false;

				// Check skips / permission
				int skipsLeft = Config.getSkipsPerDay() - questPlayer.getSkipCount();
				if (skipsLeft <= 0 && !sender.hasPermission("quests.skip")) {
					questPlayer.sendMessage(String.format("%sYou have no skips left. - Reset in %s", ChatColor.RED, StringFormatter.timeToMidnight()));
					return true;
				}

				// Player skipping his own quest by index
				// QuestPlayer -> /skipquest [index]
				return onSkipQuestByIndex(sender, questPlayer, index, clicked, clickedQuestID);

			} catch (NumberFormatException ignored) {
				// Player skipping others quest
				// Player -> /skipquest <Player>

				String targetName = args[0];
				return onSkipQuestForOther(sender, targetName, clicked, clickedQuestID, null);
			}
		}

		if (argsLen == 2) {
			// Player -> /skipquest <player> [index]

			// Check arguments
			int questIndex;
			try {
				questIndex = Integer.parseInt(args[1]) - 1;
			} catch (NumberFormatException ignored) {
				return false;
			}

			String targetName = args[0];
			return onSkipQuestForOther(sender, targetName, clicked, clickedQuestID, questIndex);

		}

		return false;
	}

	/**
	 * Called when the /skipquest command has not been executed by player via chat.
	 * @param sender the CommandSender who executed the command
	 * @param args the arguments of the command
	 * @return whether the command was used correctly
	 */
	private boolean onConsoleSkipQuest(CommandSender sender, String[] args) {
		if (args.length != 2) {
			sender.sendMessage("Use: skipquest [player] [index]");
			return true;
		}

		// Console -> /skipquest <player> [index]

		// check permission
		if (!sender.hasPermission("quests.skip.forothers")) {
			sender.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
			return true;
		}

		// Check arguments
		int index;
		try {
			index = Integer.parseInt(args[1]) - 1;
		} catch (NumberFormatException ignored) {
			sender.sendMessage("Use: skipquest [player] [index]");
			return true;
		}

		// Look for target QuestPlayer
		String targetName = args[0];
		@Nullable QuestPlayer targetPlayer = findTargetPlayer(sender, targetName);
		if (targetPlayer == null) return true;

		targetPlayer.skipQuest(index, sender);
		return true;
	}

	/** // TODO
	 * Called when a CommandSender tries to skip a QuestPlayers quest by it's index
	 * sender and target can be the same player!
	 * sender -> /skipquest <target> [questIndex]
	 * @param sender the CommandSender who executed the command
	 * @param target the QuestPlayer who's quest should be skipped
	 * @param questIndex the index of the quest that should be skipped
	 * @param clicked whether the sender has clicked on the chat to skip the quest
	 * @param clickedQuestID the ID of the clicked quest.
	 * @return whether the command was used correctly
	 */
	private boolean onSkipQuestByIndex(CommandSender sender, QuestPlayer target, int questIndex, boolean clicked, @Nullable String clickedQuestID) {
		// Check if the clicked quest is the quest at the given index
		if (target.getQuests().size() > questIndex) {
			String questID = target.getQuests().get(questIndex).getId();
			if (clicked && (questID == null || !questID.equals(clickedQuestID))) {
				sender.sendMessage(String.format("%sYou have already skipped a quest.", ChatColor.RED));
				return true;
			}
		}

		target.skipQuest(questIndex, sender);
		return true;
	}


	/**
	 * Called when a CommandSender tries to skip a players quest
	 * sender -> /skipquest <target> [questIndex]
	 * sender -> /skipquest <target>
	 * @param sender the CommandSender who executed the command
	 * @param targetName the player who's quest should be skipped
	 * @param clicked whether the sender has clicked on the chat to skip the quest
	 * @param clickedQuestID the ID of the clicked quest.
	 * @param questIndex the index of the quest that should be skipped
	 * @return whether the command was used correctly
	 */
	private boolean onSkipQuestForOther(CommandSender sender, String targetName, boolean clicked, @Nullable String clickedQuestID, @Nullable Integer questIndex) {
		// check permission
		if (!sender.hasPermission("quests.skip.forothers")) {
			sender.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
			return true;
		}

		// Find the targeted quest player
		QuestPlayer targetPlayer = findTargetPlayer(sender, targetName);
		if (targetPlayer == null) return true;

		if (questIndex != null) {
			// Select other players quest by index
			return onSkipQuestByIndex(sender, targetPlayer, questIndex, clicked, clickedQuestID);
		}

		if (sender instanceof Player) {
			// Select other players quest in chat
			promptSkipSelection((Player) sender, targetPlayer, targetName);
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
			sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, targetName));
			return null;
		}

		// Check if targeted player is QuestPlayer
		QuestPlayer targetPlayer = Main.getPlugin().getQuestPlayer(target);
		if (targetPlayer == null) {
			sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));
			return null;
		}

		return targetPlayer;
	}

	/**
	 * Shows a list of all possible quests to skip for the given player.
	 * Prompts the sender to select a quest by clicking it in the chat.
	 * A ClickEvent will be fired if a quest is clicked.
	 * This event will execute another /skipquest command with the index.
	 * @param selector the player to be prompted
	 * @param target the players who's quest should be skipped
	 * @param targetNameArgument the targets name to put in the new command. Null if selector and target are the same player.
	 */
	public void promptSkipSelection(Player selector, QuestPlayer target, @Nullable String targetNameArgument) {

		if (selector == target.getPlayer()) {
			selector.sendMessage(String.format("%S\nClick on the quest you want to skip.", ChatColor.AQUA));
		} else {
			selector.sendMessage(String.format("%S\nClick on the quest you want to skip for %s.", ChatColor.AQUA, target.getName()));
		}

		StringBuilder command = new StringBuilder("/skipquest");
		if (targetNameArgument != null) {
			command.append(" ").append(targetNameArgument);
		}

		for (int i = 0; i < target.getQuests().size(); i++) {
			Quest quest = target.getQuests().get(i);
			if (quest.getId() == null)
				quest.setId(UUID.randomUUID().toString());

			TextComponent questText = new TextComponent(String.format(" %s> %s%s", ChatColor.AQUA, ChatColor.UNDERLINE, quest.getInfo(false)));
			questText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to skip")));
			questText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (i+1) + " CLICKED " + quest.getId()));

			selector.spigot().sendMessage(questText);
		}
	}
}
