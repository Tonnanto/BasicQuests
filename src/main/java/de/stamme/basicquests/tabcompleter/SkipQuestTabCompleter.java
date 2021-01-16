package de.stamme.basicquests.tabcompleter;

import de.stamme.basicquests.main.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SkipQuestTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
		
		if (sender instanceof Player) {
			List<String> list = new ArrayList<>();

			if (command.getName().equals("skipquest") && args.length == 1 && sender.hasPermission("quests.skip.forothers")) {
				
				for (Player p: Main.plugin.getServer().getOnlinePlayers()) {
					list.add(p.getName());
				}
				
				return list;
			}
		}
		
		return null;
	}
	
}
