package de.stamme.basicquests.commands;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Gives a player all his pending quest rewards.
public class GetRewardCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		
		if (sender instanceof Player) {
			
			if (Main.plugin.questPlayer.containsKey(((Player) sender).getUniqueId())) {
				QuestPlayer questPlayer = Main.plugin.questPlayer.get(((Player) sender).getUniqueId());
				List<Quest> questsWithReward = new ArrayList<>();
				
				for (Quest quest: questPlayer.getQuests()) {
					if (quest.isCompleted() && !quest.isRewardReceived()) { questsWithReward.add(quest); }
				}
				
				if (questsWithReward.size() > 0) {
					BigDecimal moneyReward = BigDecimal.ZERO;
					int xpReward = 0;
					List<ItemStack> itemReward = new ArrayList<>();
					
					for (Quest quest: questsWithReward) {
						moneyReward = moneyReward.add(quest.getReward().getMoney());
						xpReward += quest.getReward().getXp();
						itemReward.addAll(quest.getReward().getItems());
						
						quest.setRewardReceived(true);
					}
					
					Main.log(questPlayer.getName() + " receiving " + questsWithReward.size() + " quest rewards!");
					
					if (moneyReward.compareTo(BigDecimal.ZERO) > 0) {
						EconomyResponse resp = Main.getEconomy().depositPlayer(questPlayer.getPlayer(), moneyReward.doubleValue());
						questPlayer.sendMessage(String.format("%s%s has been added to your account.", ChatColor.GREEN, Main.getEconomy().format(resp.amount)));
					}

					if (xpReward > 0) {
						questPlayer.getPlayer().giveExp(xpReward);
						questPlayer.sendMessage(String.format("%sYou have received %s XP.", ChatColor.GREEN, xpReward));
					}
					
					if (itemReward.size() > 0) {
						int inventorySize = itemReward.size() - itemReward.size() % 9 + 9;
						if (inventorySize > 54) { inventorySize = 54; }
						Inventory inventory = Bukkit.createInventory(null, inventorySize, String.format("%s%sReward!",  ChatColor.BOLD,  ChatColor.LIGHT_PURPLE));

						for (ItemStack i: itemReward) {
							inventory.addItem(i);
						}

						questPlayer.getPlayer().openInventory(inventory);
						questPlayer.setRewardInventory(inventory);
						questPlayer.sendMessage(String.format("%s%sReward-Inventory opened!", ChatColor.GREEN, ChatColor.BOLD));
					}
					
					questPlayer.receiveNewQuests();
					
				} else
					questPlayer.sendMessage(String.format("%sNo Rewards available!", ChatColor.RED));
				
			} else
				sender.sendMessage(String.format("%sNo Rewards available!", ChatColor.RED));
			
			return true;
			
		}
		
		return false;
	}
}
