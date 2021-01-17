package de.stamme.basicquests.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

// Lists a player's current quests in the chat
public class QuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		
		if (sender instanceof Player) {
			
			if (Main.plugin.questPlayer.containsKey(((Player) sender).getUniqueId())) {
				QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());
				
				if (player.quests.size() > 1) {
					
					if (args.length > 0) {
						// /quests detail for more detail
						if (args.length == 1 && args[0].equals("detail")) {
							StringBuilder message = new StringBuilder("\nYour Quests and Rewards:");
							for (int i = 0; i < player.quests.size(); i++) {
								Quest q = player.quests.get(i);
								if (i != 0)
									message.append("\n ");

								message.append(String.format("\n %s>%s %s", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(true)));
							}
							player.sendMessage(message.toString());

						} else
							return false;
						
					} else {

						ComponentBuilder message = new ComponentBuilder("\nYour Quests:  ");
						TextComponent showRewardsButton = new TextComponent(String.format("%s>> Show Rewards <<", ChatColor.AQUA));
						showRewardsButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to show rewards.")));
						showRewardsButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests detail"));
						message.append(showRewardsButton);
						player.player.spigot().sendMessage(message.create());

						for (Quest q: player.quests) {
							player.sendMessage(String.format(" %s>%s %s", ChatColor.GOLD, ChatColor.WHITE, q.getInfo(false)));
						}
					}

				} else
					player.sendMessage(ChatColor.RED + "No Quests found!");
				
			} else
				sender.sendMessage(ChatColor.RED + "No Quests found!");
			
			return true;
			
		}
		
		return false;
	}
}
