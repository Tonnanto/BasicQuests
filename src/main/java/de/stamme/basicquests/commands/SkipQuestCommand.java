package de.stamme.basicquests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class SkipQuestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

		if (args.length <= 0)
			return false;

		int index;
		try {
			index = Integer.parseInt(args[0]) - 1;
		} catch (NumberFormatException e) {
			return false;
		}

		int argsLen = args.length;

		// TODO: Implement Confirmation
//		// Ask player to confirm the action
//		if (sender instanceof Player && !args[args.length-1].equalsIgnoreCase("CONFIRM")) {
//			QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());
//			if (player != null) {
//				player.sendMessage(String.format("You are about to skip this quest: \n", ));
//			}
//
//
//		} else
//			argsLen--;

		if (argsLen == 2) {
			// /skipquest [index] <player>

			String playerName = args[1];
			Player target = Main.plugin.getServer().getPlayer(playerName);

			if ((target != sender) && !sender.hasPermission("quests.skip.forothers")) {
				sender.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
				return true;
			}

			if (target != null) {
				QuestPlayer targetPlayer = Main.plugin.questPlayer.get(target.getUniqueId());
				if (targetPlayer != null) {
					targetPlayer.skipQuest(index, target != sender);

				} else
					sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));

			} else
				sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));

			return true;

		} else if (sender instanceof Player && argsLen == 1) {
			// /skipquest [index]
			if (Main.plugin.questPlayer.containsKey(((Player) sender).getUniqueId())) {
				QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());
				player.skipQuest(index, false);
				return true;
			} else
				sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));
		} else
			return false;
		
		return true;
	}
}
