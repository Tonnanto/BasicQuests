package de.stamme.basicquests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class ResetQuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

		// Check for permission
		if (!sender.hasPermission("quests.reset")) {
			sender.sendMessage(String.format("%sYou are not allowed to use this command.", ChatColor.RED));
			return true;
		}

		if (args.length > 0) {
			if (args.length == 1) {
				// /resetquests <player>

				String playerName = args[0];
				Player targetPlayer = Main.plugin.getServer().getPlayer(playerName);

				if (targetPlayer != sender && !sender.hasPermission("quests.reset.forothers")) {
					sender.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
					return true;
				}

				QuestPlayer target = null;

				if (targetPlayer != null)
					target = Main.plugin.questPlayer.get(targetPlayer.getUniqueId());

				if (target != null) {
					target.resetQuests();
					target.getPlayer().sendMessage(String.format("%sYour quests have been reset.", ChatColor.GREEN));
				} else
					sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));

				return true;

			} else
				return false;

		} else if (sender instanceof Player) {
			if (Main.plugin.questPlayer.containsKey(((Player) sender).getUniqueId())) {
				QuestPlayer questPlayer = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());

				if (questPlayer != null) {
					questPlayer.resetQuests();
					questPlayer.sendMessage(String.format("%sYour quests have been reset.", ChatColor.GREEN));
				} else
					sender.sendMessage(String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED));

				return true;
			}
		}
		
		return false;
	}
	

}
