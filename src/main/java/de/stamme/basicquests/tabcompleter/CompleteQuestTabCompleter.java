package de.stamme.basicquests.tabcompleter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;

public class CompleteQuestTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		
		if (sender instanceof Player) {
			QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());
			List<String> list = new ArrayList<>();
		
			if (player != null && command.getName().equals("completequest") && args.length == 1 && player.hasPermission("quests.complete")) {
				
				if (player.quests == null) { return null; }
				
				for (int i = 1; i <= player.quests.size(); i++) {
					list.add(""+i);
				}
				
				return list;
				
				
			} else if (command.getName().equals("completequest") && args.length == 2 && sender.hasPermission("quests.complete.forothers")) {
				
				for (Player p: Main.plugin.getServer().getOnlinePlayers()) {
					list.add(p.getName());
				}
				
				return list;
			}
		}
		
		return null;
	}
}
