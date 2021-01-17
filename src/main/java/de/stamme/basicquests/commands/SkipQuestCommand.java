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

import java.util.UUID;

public class SkipQuestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

		if (sender instanceof Player) {
			QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());

			int argsLen = args.length;

			boolean clicked = false;
			String clickedQuestID = "";
			if (args.length > 1 && args[args.length-2].equalsIgnoreCase("CLICKED")) {
				clicked = true;
				clickedQuestID = args[args.length-1];
				argsLen -= 2;
			}


			if (player != null) {
				int skipsLeft = Config.getSkipsPerDay() - player.getSkipCount();

				if (argsLen == 0) {
					// Player -> /skipquest

					// Check skips / permission
					if (!player.hasPermission("quests.skip") && skipsLeft <= 0) {
						player.sendMessage(String.format("%sYou have no skips left. - Reset in %s", ChatColor.RED, StringFormatter.timeToMidnight()));
						return true;
					}

					// Prompt to select quest in chat
					promptSkipSelection(player, player, args);


				} else if (argsLen == 1) {
					// Check argument
					try {
						int index = Integer.parseInt(args[0]) - 1;

						// Check if the clicked quest is the quest at the given index
						if (player.quests.size() > index) {
							String questID = player.quests.get(index).id;
							if (clicked && (questID == null || !questID.equals(clickedQuestID))) {
								sender.sendMessage(String.format("%sYou have already skipped this quest.", ChatColor.RED));
								return true;
							}
						}

						// Player -> /skipquest [index]
						player.skipQuest(index, sender);


					} catch (NumberFormatException ignored) {
						// Player -> /skipquest <Player>
						// check permission
						if (!player.hasPermission("quests.skip.forothers")) {
							sender.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
							return true;
						}
						String playerName = args[0];
						Player target = Main.plugin.getServer().getPlayer(playerName);

						if (target != null) {
							QuestPlayer targetPlayer = Main.plugin.questPlayer.get(target.getUniqueId());
							if (targetPlayer != null) {
								// Prompt to select in chat
								promptSkipSelection(player, targetPlayer, args);

							} else
								sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));
						} else
							sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));

					}

				} else if (argsLen == 2) {
					// Player -> /skipquest <player> [index]

					// check permission
					if (!player.hasPermission("quests.skip.forothers")) {
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
									sender.sendMessage(String.format("%sYou have already skipped this quest.", ChatColor.RED));
									return true;
								}
							}
							targetPlayer.skipQuest(index, sender);

						} else
							sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));
					} else
						sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));

				} else
					return false;
			}



		} else if (args.length == 2) {
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

			String playerName = args[0];
			Player target = Main.plugin.getServer().getPlayer(playerName);

			if (target != null) {
				QuestPlayer targetPlayer = Main.plugin.questPlayer.get(target.getUniqueId());
				if (targetPlayer != null)
					targetPlayer.skipQuest(index, sender);
				else
					sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));
			} else
				sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));
		} else
			sender.sendMessage("Use: skipquest [player] [index]");

		return true;
	}

	public void promptSkipSelection(QuestPlayer selector, QuestPlayer target, String[] args) {

		if (selector == target) {
			selector.sendMessage(String.format("%S\nClick on the quest you want to skip.", ChatColor.AQUA));
			if (!selector.hasPermission("quests.skip")) {
				selector.sendMessage(String.format("%sYou have %s skip%s left for today.", ChatColor.RED, selector.getSkipsLeft(), (selector.getSkipsLeft() == 1) ? "" : "s"));
			}

		} else {
			selector.sendMessage(String.format("%S\nClick on the quest you want to skip for %s.", ChatColor.AQUA, target.getName()));
		}

		StringBuilder command = new StringBuilder("/skipquest");
		for (String arg: args) {
			command.append(" ").append(arg);
		}

		for (int i = 0; i < target.quests.size(); i++) {
			Quest quest = target.quests.get(i);
			if (quest.id == null)
				quest.id = UUID.randomUUID().toString();

			TextComponent questText = new TextComponent(String.format(" %s> %s%s", ChatColor.AQUA, ChatColor.UNDERLINE, quest.getInfo(false)));
			questText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to skip")));
			questText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command.toString() + " " + (i+1) + " CLICKED " + quest.id));

			selector.player.spigot().sendMessage(questText);
		}
	}
}
