package de.stamme.basicquests.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QuestsTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		
		if (sender instanceof Player) {

			if (command.getName().equals("quests") && args.length == 1) {
				List<String> list = new ArrayList<>();
				list.add("detail");
				return list;
			}
		}
		
		return null;
	}

}
