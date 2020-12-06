package de.stamme.basicquests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.Quest;
import net.md_5.bungee.api.ChatColor;

// Lists a player's current quests in the chat
public class QuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (sender instanceof Player) {
			
			if (Main.plugin.questPlayer.containsKey(((Player) sender).getUniqueId())) {
				QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getPlayer().getUniqueId());
				
				if (player.quests.size() > 1) {
					String message = "Current Quests: \n";
					
					if (args.length > 0) {
						// /quests detail for more detail
						if (args.length == 1 && args[0].equals("detail")) {
							for (Quest q: player.quests) {
								message += String.format(" %s>%s %s\n", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(true));
							}
						} else
							return false;
						
					} else {
						for (Quest q: player.quests) {
							message += String.format(" %s>%s %s\n", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(false));
						}
					}
					
					
					
					player.sendMessage(message);
					
				} else
					player.sendMessage(ChatColor.RED + "No Quests found!");
				
			} else
				((Player) sender).sendMessage(ChatColor.RED + "No Quests found!");
			
			return true;
			
		}
		
		return false;
	}

}
