package de.stamme.basicquests.commands.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class QuestsTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		
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
