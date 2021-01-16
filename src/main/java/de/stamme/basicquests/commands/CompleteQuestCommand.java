package de.stamme.basicquests.commands;

import de.stamme.basicquests.data.Config;
import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.util.StringFormatter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class CompleteQuestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

		// Check for permission
		if (!sender.hasPermission("quests.complete")) {
			sender.sendMessage(String.format("%sYou are not allowed to use this command.", ChatColor.RED));
			return true;
		}

		int argsLen = args.length;

		boolean clicked = false;
		String clickedQuestID = "";
		if (args.length > 1 && args[args.length-2].equalsIgnoreCase("CLICKED")) {
			clicked = true;
			clickedQuestID = args[args.length-1];
			argsLen -= 2;
		}

		if (sender instanceof Player) {
			QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());
			int skipsLeft = Config.getSkipsPerDay() - player.getSkipCount();


			if (player != null) {
				if (argsLen == 0) {
					// Player -> /completequest

					// Prompt to select quest in chat
					promptCompleteSelection(player, player.quests, args);


				} else if (argsLen == 1) {
					// Check argument
					try {
						int index = Integer.parseInt(args[0]) - 1;

						// Check if the clicked quest is the quest at the given index
						if (player.quests.size() > index) {
							String questID = player.quests.get(index).id;
							if (clicked && (questID == null || !questID.equals(clickedQuestID))) {
								sender.sendMessage(String.format("%sYou have already completed a quest.", ChatColor.RED));
								return true;
							}
						}

						// Player -> /completequest [index]
						player.completeQuest(index);


					} catch (NumberFormatException ignored) {
						// Player -> /completequest <Player>
						// check permission
						if (!player.hasPermission("quests.complete.forothers")) {
							sender.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
							return true;
						}
						String playerName = args[0];
						Player target = Main.plugin.getServer().getPlayer(playerName);

						if (target != null) {
							QuestPlayer targetPlayer = Main.plugin.questPlayer.get(target.getUniqueId());
							if (targetPlayer != null) {
								// Prompt to select in chat
								promptCompleteSelection(player, targetPlayer.quests, args);

							} else
								sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));

						} else
							sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));

					}

				} else if (argsLen == 2) {
					// Player -> /completequest <player> [index]

					// check permission
					if (!player.hasPermission("quests.complete.forothers")) {
						sender.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
						return true;
					}

					// Check arguments
					int index;
					try {
						index = Integer.parseInt(args[1]) - 1;
					} catch (NumberFormatException ignored) {
						return false;
					}

					String playerName = args[0];
					Player target = Main.plugin.getServer().getPlayer(playerName);

					if (target != null) {
						QuestPlayer targetPlayer = Main.plugin.questPlayer.get(target.getUniqueId());
						if (targetPlayer != null) {

							// Check if the clicked quest is the quest at the given index
							if (targetPlayer.quests.size() > index) {
								String questID = targetPlayer.quests.get(index).id;
								if (clicked && (questID == null || !questID.equals(clickedQuestID))) {
									sender.sendMessage(String.format("%sYou have already completed a quest.", ChatColor.RED));
									return true;
								}
							}

							if (targetPlayer.completeQuest(index))
								sender.sendMessage(String.format("%sThe %s quest has been completed for %s", ChatColor.GREEN, index + 1, targetPlayer.player.getName()));
							else
								sender.sendMessage(String.format("%sFailed to complete quest", ChatColor.RED));



						} else
							sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));

					} else
						sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));

				} else
					return false;
			}



		} else if (args.length == 2) {
			// Console -> /completequest <player> [index]

			// check permission
			if (!sender.hasPermission("quests.complete.forothers")) {
				sender.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
				return true;
			}

			// Check arguments
			int index;
			try {
				index = Integer.parseInt(args[1]) - 1;
			} catch (NumberFormatException ignored) {
				sender.sendMessage("Use: completequest [player] [index]");
				return true;
			}

			String playerName = args[0];
			Player target = Main.plugin.getServer().getPlayer(playerName);

			if (target != null) {
				QuestPlayer targetPlayer = Main.plugin.questPlayer.get(target.getUniqueId());
				if (targetPlayer != null) {
					if (targetPlayer.quests != null && targetPlayer.quests.size() > index && index >= 0)
						if (targetPlayer.completeQuest(index))
							sender.sendMessage(String.format("%sThe %s quest has been completed for %s", ChatColor.GREEN, index + 1, targetPlayer.player.getName()));
						else
							sender.sendMessage(String.format("%sFailed to complete quest", ChatColor.RED));
					else
						sender.sendMessage(String.format("%sNo quest found at index %s.", ChatColor.RED, index + 1));
				} else
					sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));
			} else
				sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));


		} else {
			sender.sendMessage("Use: completequest [player] [index]");
		}

		return true;
	}

	public void promptCompleteSelection(QuestPlayer selector, ArrayList<Quest> quests, String[] args) {

		selector.sendMessage(String.format("%S\nClick on the quest you want to complete.", ChatColor.AQUA));

		StringBuilder command = new StringBuilder("/completequest");
		for (String arg: args) {
			command.append(" ").append(arg);
		}

		for (int i = 0; i < quests.size(); i++) {
			Quest quest = quests.get(i);
			if (quest.id == null)
				quest.id = UUID.randomUUID().toString();

			TextComponent questText = new TextComponent(String.format(" %s> %s%s", ChatColor.AQUA, ChatColor.UNDERLINE, quest.getInfo(false)));
			questText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to complete")));
			questText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command.toString() + " " + (i+1) + " CLICKED " + quest.id));

			selector.player.spigot().sendMessage(questText);
		}
	}
}
