package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.config.MessagesConfig;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RewardCommand extends BasicQuestsCommand {

    public RewardCommand() {
        super("reward");
    }

    @Override
    public void evaluate(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        if (!(sender instanceof Player))
            return;

        QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer((Player) sender);
        if (questPlayer == null) {
            BasicQuestsPlugin.sendMessage(sender, buildNoRewardAvailableMessage());
            return;
        }

        // Get Quests with pending rewards
        List<Quest> questsWithReward = new ArrayList<>();
        for (Quest quest: questPlayer.getQuests()) {
            if (quest.isCompleted() && !quest.isRewardReceived())
                questsWithReward.add(quest);
        }

        if (questsWithReward.size() == 0) {
            BasicQuestsPlugin.sendMessage(sender, buildNoRewardAvailableMessage());
            return;
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

        BasicQuestsPlugin.log(MessageFormat.format(MessagesConfig.getMessage("log.playerReceivedRewards"), questPlayer.getName(), questsWithReward.size()));

        // Receive Rewards
        receiveMoneyReward(questPlayer, moneyReward);
        receiveXpReward(questPlayer, xpReward);
        receiveItemReward(questPlayer, itemReward);

        // New Quests
        questPlayer.receiveNewQuests();
    }

    void receiveMoneyReward(QuestPlayer questPlayer, BigDecimal moneyReward) {
        if (moneyReward.compareTo(BigDecimal.ZERO) <= 0) return;
        EconomyResponse resp = BasicQuestsPlugin.getEconomy().depositPlayer(questPlayer.getPlayer(), moneyReward.doubleValue());
        questPlayer.sendMessage(MessageFormat.format(MessagesConfig.getMessage("rewards.moneyRewardReceived"), BasicQuestsPlugin.getEconomy().format(resp.amount)));
    }

    void receiveXpReward(QuestPlayer questPlayer, int xpReward) {
        if (xpReward <= 0) return;
        questPlayer.getPlayer().giveExp(xpReward);
        questPlayer.sendMessage(MessageFormat.format(MessagesConfig.getMessage("rewards.xpRewardReceived"), xpReward));
    }

    void receiveItemReward(QuestPlayer questPlayer, List<ItemStack> itemReward) {
        if (itemReward.size() == 0) return;

        // Calculate the number of inventory slots needed
        // ItemStack.amount can be higher than 64. This leads to taking more than one slot in the inventory.
        Optional<Integer> actualItemStacks = itemReward.stream().map(itemStack -> (itemStack.getAmount() / 64) + 1).reduce(Integer::sum);
        int inventorySize = actualItemStacks.get() - (actualItemStacks.get() % 9) + 9;
        if (inventorySize > 54) { inventorySize = 54; }

        String rewardInventoryTitle = MessagesConfig.getMessage("rewards.rewardInventoryTitle");
        Inventory inventory = Bukkit.createInventory(null, inventorySize, rewardInventoryTitle);

        for (ItemStack i: itemReward) {
            inventory.addItem(i);
        }

        questPlayer.getPlayer().openInventory(inventory);
        questPlayer.setRewardInventory(inventory);
        questPlayer.sendMessage(MessagesConfig.getMessage("rewards.itemRewardReceived"));
    }

    String buildNoRewardAvailableMessage() {
        return MessagesConfig.getMessage("rewards.noRewardAvailable");
    }
}
