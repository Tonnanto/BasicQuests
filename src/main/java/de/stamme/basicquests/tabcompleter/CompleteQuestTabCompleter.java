package de.stamme.basicquests.tabcompleter;

import de.stamme.basicquests.main.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CompleteQuestTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
		
		if (sender instanceof Player) {
			List<String> list = new ArrayList<>();
		
			if (command.getName().equals("completequest") && args.length == 1 && sender.hasPermission("quests.complete.forothers")) {
				
				for (Player p: Main.getPlugin().getServer().getOnlinePlayers()) {
					list.add(p.getName());
				}
				
				return list;
			}
		}
		
		return null;
	}
}
