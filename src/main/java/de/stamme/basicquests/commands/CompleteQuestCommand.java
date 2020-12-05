package de.stamme.basicquests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import net.md_5.bungee.api.ChatColor;

public class CompleteQuestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (sender instanceof Player) {
			if (Main.plugin.questPlayer.containsKey(((Player) sender).getUniqueId())) {
				QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getPlayer().getUniqueId());
				// Check for permission
				if (!player.hasPermission("quests.complete")) {
					player.sendMessage(String.format("You are not allowed to use this command.", ChatColor.RED));
					return true;
				}
				
				
				if (args.length > 0) {
					
					int index;
					try {
						index = Integer.parseInt(args[0]) - 1;
					} catch (NumberFormatException e) {
						return false;
					}
					
					
					if (args.length == 1) {
						// /completequest [index]
						player.completeQuest(index);
						return true;
						
					} else if (args.length == 2) {
						// /completequest [index] <player>
						if (player.hasPermission("quests.complete.forothers")) {
							
							String playerName = args[1];
							Player target = Main.plugin.getServer().getPlayer(playerName);
							
							if (target != null) {
								QuestPlayer targetPlayer = Main.plugin.questPlayer.get(target.getUniqueId());
								if (targetPlayer != null) {
									targetPlayer.completeQuest(index);
									return true;
								}
								
							} else
								player.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));
							
						} else
							player.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
						
						return true;
							
					} else
						return false;
				} else
					return false;
			}
		}
		
		return false;
	}
}
