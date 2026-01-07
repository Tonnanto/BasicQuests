package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.rewards.RewardItem;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class RewardCommand extends BasicQuestsCommand {

  public RewardCommand() {
    super("reward");
  }

  @Override
  public final @NotNull String getPermission() {
    return "basicquests.use.reward";
  }

  @Override
  public void evaluate(
      @NotNull BasicQuestsPlugin plugin,
      @NotNull CommandSender sender,
      @NotNull String alias,
      @NotNull @Unmodifiable List<String> params) {
    if (!(sender instanceof Player)) return;

    QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer((Player) sender);
    if (questPlayer == null) {
      BasicQuestsPlugin.sendMessage(sender, buildNoRewardAvailableMessage());
      return;
    }

    // Get Quests with pending rewards
    List<Quest> questsWithReward = new ArrayList<>();
    for (Quest quest : questPlayer.getQuests()) {
      if (quest.isCompleted() && !quest.isRewardReceived()) questsWithReward.add(quest);
    }

    if (questsWithReward.isEmpty()) {
      // List quests and rewards if no rewards are pending
      plugin.getServer().dispatchCommand(sender, "quests list rewards");
      return;
    }
    // Rewards available

    BigDecimal moneyReward = BigDecimal.ZERO;
    int xpReward = 0;
    List<RewardItem> itemReward = new ArrayList<>();

    // Accumulate rewards
    for (Quest quest : questsWithReward) {
      moneyReward = moneyReward.add(quest.getReward().getMoney());
      xpReward += quest.getReward().getXp();
      itemReward.addAll(quest.getReward().getRewardItems());

      quest.setRewardReceived(true);
    }

    BasicQuestsPlugin.log(
        MessageFormat.format(
            MessagesConfig.getMessage("events.log.received-rewards"),
            questPlayer.getName(),
            questsWithReward.size()));

    // Receive Rewards
    receiveMoneyReward(questPlayer, moneyReward);
    receiveXpReward(questPlayer, xpReward);
    receiveItemReward(questPlayer, itemReward);

    // New Quests
    questPlayer.receiveNewQuests();
  }

  void receiveMoneyReward(QuestPlayer questPlayer, BigDecimal moneyReward) {
    if (moneyReward.compareTo(BigDecimal.ZERO) <= 0) return;
    EconomyResponse resp =
        BasicQuestsPlugin.getEconomy()
            .depositPlayer(questPlayer.getPlayer(), moneyReward.doubleValue());
    questPlayer.sendMessage(
        MessageFormat.format(
            MessagesConfig.getMessage("commands.reward.rewards.money"),
            BasicQuestsPlugin.getEconomy().format(resp.amount)));
  }

  void receiveXpReward(QuestPlayer questPlayer, int xpReward) {
    if (xpReward <= 0) return;
    questPlayer.getPlayer().giveExp(xpReward);
    questPlayer.sendMessage(
        MessageFormat.format(MessagesConfig.getMessage("commands.reward.rewards.xp"), xpReward));
  }

  void receiveItemReward(QuestPlayer questPlayer, List<RewardItem> rewardItems) {
    if (rewardItems.isEmpty()) return;

    // Calculate the number of inventory slots needed
    // RewardItem.amount can be higher than 64. This leads to taking more than one slot in the
    // inventory.
    Optional<Integer> actualItemStacks =
        rewardItems.stream().map(rewardItem -> (rewardItem.amount / 64) + 1).reduce(Integer::sum);
    int inventorySize = actualItemStacks.get() - (actualItemStacks.get() % 9) + 9;
    if (inventorySize > 54) {
      inventorySize = 54;
    }

    String rewardInventoryTitle = MessagesConfig.getMessage("commands.reward.inventory-title");
    Inventory inventory = Bukkit.createInventory(null, inventorySize, rewardInventoryTitle);

    for (RewardItem rewardItem : rewardItems) {
      ItemStack rewardItemStack = rewardItem.item;
      rewardItemStack.setAmount(rewardItem.amount);
      inventory.addItem(rewardItemStack);
    }

    questPlayer.getPlayer().openInventory(inventory);
    questPlayer.setRewardInventory(inventory);
    questPlayer.sendMessage(MessagesConfig.getMessage("commands.reward.rewards.item"));
  }

  String buildNoRewardAvailableMessage() {
    return MessagesConfig.getMessage("commands.reward.none");
  }
}
