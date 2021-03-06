package de.stamme.basicquests.tabcompleter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import org.jetbrains.annotations.NotNull;

public class ResetQuestsTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		
		if (sender instanceof Player) {
		
			if (command.getName().equals("resetquests") && args.length == 1) {
				List<String> list = new ArrayList<>();
				
				for (Player p: Main.plugin.getServer().getOnlinePlayers()) {
					list.add(p.getName());
				}
				
				return list;
			}
		}
		
		return null;
	}
	
}
