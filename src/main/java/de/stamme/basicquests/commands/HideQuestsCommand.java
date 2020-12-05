package de.stamme.basicquests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.main.QuestsScoreBoardManager;

public class HideQuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (sender instanceof Player) {
			if (Main.plugin.questPlayer.containsKey(((Player) sender).getUniqueId())) {
				QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getPlayer().getUniqueId());
				QuestsScoreBoardManager.hide(player);
			}
			
			return true;
		}
		
		return false;
	}

}
