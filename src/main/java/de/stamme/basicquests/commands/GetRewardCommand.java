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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Gives a player all his pending quest rewards.
 */
public class GetRewardCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) return true;

		QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer((Player) sender);
		if (questPlayer == null) {
			sender.sendMessage(buildNoRewardAvailableMessage());
			return true;
		}

		// Get Quests with pending rewards
		List<Quest> questsWithReward = new ArrayList<>();
		for (Quest quest: questPlayer.getQuests()) {
			if (quest.isCompleted() && !quest.isRewardReceived())
				questsWithReward.add(quest);
		}

		if (questsWithReward.size() == 0) {
			sender.sendMessage(buildNoRewardAvailableMessage());
			return true;
		}
		// Rewards available

		BigDecimal moneyReward = BigDecimal.ZERO;
		int xpReward = 0;
		List<ItemStack> itemReward = new ArrayList<>();

		// Accumulate rewards
		for (Quest quest: questsWithReward) {
			moneyReward = moneyReward.add(quest.getReward().getMoney());
			xpReward += quest.getReward().getXp();
			itemReward.addAll(quest.getReward().getItems());

			quest.setRewardReceived(true);
		}

		Main.log(MessageFormat.format(Main.l10n("log.playerReceivedRewards"), questPlayer.getName(), questsWithReward.size()));

		// Receive Rewards
		receiveMoneyReward(questPlayer, moneyReward);
		receiveXpReward(questPlayer, xpReward);
		receiveItemReward(questPlayer, itemReward);

		// New Quests
		questPlayer.receiveNewQuests();
		return true;
	}

	void receiveMoneyReward(QuestPlayer questPlayer, BigDecimal moneyReward) {
		if (moneyReward.compareTo(BigDecimal.ZERO) <= 0) return;
		EconomyResponse resp = Main.getEconomy().depositPlayer(questPlayer.getPlayer(), moneyReward.doubleValue());
		questPlayer.sendMessage(ChatColor.GREEN + MessageFormat.format(Main.l10n("rewards.moneyRewardReceived"), Main.getEconomy().format(resp.amount)));
	}

	void receiveXpReward(QuestPlayer questPlayer, int xpReward) {
		if (xpReward <= 0) return;
		questPlayer.getPlayer().giveExp(xpReward);
		questPlayer.sendMessage(ChatColor.GREEN + MessageFormat.format(Main.l10n("rewards.xpRewardReceived"), xpReward));
	}

	void receiveItemReward(QuestPlayer questPlayer, List<ItemStack> itemReward) {
		if (itemReward.size() == 0) return;

		int inventorySize = itemReward.size() - itemReward.size() % 9 + 9;
		if (inventorySize > 54) { inventorySize = 54; }
		String rewardInventoryTitle = ChatColor.BOLD + ChatColor.LIGHT_PURPLE.toString() + Main.l10n("rewards.rewardInventoryTitle");
		Inventory inventory = Bukkit.createInventory(null, inventorySize, rewardInventoryTitle);

		for (ItemStack i: itemReward) {
			inventory.addItem(i);
		}

		questPlayer.getPlayer().openInventory(inventory);
		questPlayer.setRewardInventory(inventory);
		questPlayer.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + Main.l10n("rewards.itemRewardReceived"));
	}

	String buildNoRewardAvailableMessage() {
		return ChatColor.RED + Main.l10n("rewards.noRewardAvailable");
	}
}
