package de.stamme.basicquests.commands;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

// Gives a player all his pending quest rewards.
public class GetRewardCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (sender instanceof Player) {
			
			if (Main.plugin.questPlayer.containsKey(((Player) sender).getUniqueId())) {
				QuestPlayer player = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());
				List<Quest> questsWithReward = new ArrayList<>();
				
				for (Quest q: player.quests) {
					if (q.completed() && !q.rewardReceived) { questsWithReward.add(q); }
				}
				
				if (questsWithReward.size() > 0) {
					BigDecimal moneyReward = BigDecimal.ZERO;
					List<ItemStack> itemReward = new ArrayList<>();
					
					for (Quest q: questsWithReward) {
						moneyReward = moneyReward.add(q.reward.money);
						itemReward.addAll(q.reward.items);
						
						q.rewardReceived = true;
					}
					
					Main.log(player.getName() + " recieving " + questsWithReward.size() + " quest rewards!");
					
					if (moneyReward.compareTo(BigDecimal.ZERO) > 0) {
						EconomyResponse resp = Main.getEconomy().depositPlayer(player.player, moneyReward.doubleValue());
						player.sendMessage(String.format("%s%s has been added to your account.", ChatColor.GREEN, Main.getEconomy().format(resp.amount)));
					}
					
					if (itemReward.size() > 0) {
						int inventorySize = itemReward.size() - itemReward.size() % 9 + 9;
						if (inventorySize > 54) { inventorySize = 54; }
						Inventory inventory = Bukkit.createInventory(null, inventorySize, String.format("%s%sReward!",  ChatColor.BOLD,  ChatColor.LIGHT_PURPLE));
						
						for (ItemStack i: itemReward) {
							inventory.addItem(i);
						}
						
						player.player.openInventory(inventory);
						player.sendMessage(String.format("%s%sReward-Invetory opened!", ChatColor.GREEN, ChatColor.BOLD));
					}
					
					player.recieveNewQuests();
					
				} else
					player.sendMessage(String.format("%sNo Rewards available!", ChatColor.RED));
				
			} else
				sender.sendMessage(String.format("%sNo Rewards available!", ChatColor.RED));
			
			return true;
			
		}
		
		return false;
	}
}
