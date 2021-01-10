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
		
		if (sender instanceof Player) {
			if (Main.plugin.questPlayer.containsKey(((Player) sender).getUniqueId())) {
				QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());
			
				// Check for permission
				if (!player.hasPermission("quests.reset")) {
					player.sendMessage(String.format("%sYou are not allowed to use this command.", ChatColor.RED));
					return true;
				}
				
				if (args.length > 0) {
					// /resetquests <player>
					if (args.length == 1) {
						String playerName = args[0];

						Player targetPlayer = Main.plugin.getServer().getPlayer(playerName);
						QuestPlayer target = null;

						if (targetPlayer != null) {
							target = Main.plugin.questPlayer.get(targetPlayer.getUniqueId());
						}
						
						if (target != null) {
							
							target.resetQuests();
							target.player.sendMessage(String.format("%sYour quests have been reset.", ChatColor.GREEN));
							
						} else
							player.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, playerName));
						
					} else
						return false;
					
				} else {
					
					QuestPlayer questPlayer = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());
					if (questPlayer != null) {
						questPlayer.resetQuests();
						player.sendMessage(String.format("%sYour quests have been reset.", ChatColor.GREEN));
					}
				}
					
				
				return true;
			}
		}
		
		return false;
	}
	

}
