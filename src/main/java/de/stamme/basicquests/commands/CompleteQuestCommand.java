package de.stamme.basicquests.commands;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.Quest;
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
			QuestPlayer questPlayer = Main.getPlugin().getQuestPlayers().get(((Player) sender).getUniqueId());

			if (questPlayer != null) {
				if (argsLen == 0) {
					// Player -> /completequest

					// Prompt to select quest in chat
					promptCompleteSelection(questPlayer, questPlayer, args);

				} else if (argsLen == 1) {
					// Check argument
					try {
						int index = Integer.parseInt(args[0]) - 1;

						// Check if the clicked quest is the quest at the given index
						if (questPlayer.getQuests().size() > index) {
							String questID = questPlayer.getQuests().get(index).getId();
							if (clicked && (questID == null || !questID.equals(clickedQuestID))) {
								sender.sendMessage(String.format("%sYou have already completed a quest.", ChatColor.RED));
								return true;
							}
						}

						// Player -> /completequest [index]
						questPlayer.completeQuest(index, sender);


					} catch (NumberFormatException ignored) {
						// Player -> /completequest <Player>
						// check permission
						if (!questPlayer.hasPermission("quests.complete.forothers")) {
							sender.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
							return true;
						}
						String playerName = args[0];
						Player target = Main.getPlugin().getServer().getPlayer(playerName);

						if (target != null) {
							QuestPlayer targetPlayer = Main.getPlugin().getQuestPlayers().get(target.getUniqueId());
							if (targetPlayer != null) {
								// Prompt to select in chat
								promptCompleteSelection(questPlayer, targetPlayer, args);

							} else
								sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));
						} else
							sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));
					}

				} else if (argsLen == 2) {
					// Player -> /completequest <player> [index]

					// check permission
					if (!questPlayer.hasPermission("quests.complete.forothers")) {
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
					Player target = Main.getPlugin().getServer().getPlayer(playerName);

					if (target != null) {
						QuestPlayer targetPlayer = Main.getPlugin().getQuestPlayers().get(target.getUniqueId());
						if (targetPlayer != null) {

							// Check if the clicked quest is the quest at the given index
							if (targetPlayer.getQuests().size() > index) {
								String questID = targetPlayer.getQuests().get(index).getId();
								if (clicked && (questID == null || !questID.equals(clickedQuestID))) {
									sender.sendMessage(String.format("%sYou have already completed a quest.", ChatColor.RED));
									return true;
								}
							}
							targetPlayer.completeQuest(index, sender);

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
			Player target = Main.getPlugin().getServer().getPlayer(playerName);

			if (target != null) {
				QuestPlayer targetPlayer = Main.getPlugin().getQuestPlayers().get(target.getUniqueId());
				if (targetPlayer != null) {
					targetPlayer.completeQuest(index, sender);
				} else
					sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));
			} else
				sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));
		} else
			sender.sendMessage("Use: completequest [player] [index]");

		return true;
	}

	public void promptCompleteSelection(QuestPlayer selector, QuestPlayer target, String[] args) {

		if (selector == target) {
			selector.sendMessage(String.format("%S\nClick on the quest you want to complete.", ChatColor.AQUA));

		} else {
			selector.sendMessage(String.format("%S\nClick on the quest you want to complete for %s.", ChatColor.AQUA, target.getName()));

		}


		StringBuilder command = new StringBuilder("/completequest");
		for (String arg: args) {
			command.append(" ").append(arg);
		}

		for (int i = 0; i < target.getQuests().size(); i++) {
			Quest quest = target.getQuests().get(i);
			if (quest.getId() == null)
				quest.setId(UUID.randomUUID().toString());

			TextComponent questText = new TextComponent(String.format(" %s> %s%s", ChatColor.AQUA, ChatColor.UNDERLINE, quest.getInfo(false)));
			questText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to complete")));
			questText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (i+1) + " CLICKED " + quest.getId()));

			selector.getPlayer().spigot().sendMessage(questText);
		}
	}
}
