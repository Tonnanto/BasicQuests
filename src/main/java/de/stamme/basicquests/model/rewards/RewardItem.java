package de.stamme.basicquests.model.rewards;

import java.io.Serializable;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an Item that is about to be included in a reward along with an estimated value of the
 * item reward. The value helps identify an appropriate value for the remaining reward.
 */
public class RewardItem implements Comparable<RewardItem>, Serializable {
  public final ItemStack item;
  public final int amount;
  public final double value;

  public RewardItem(ItemStack item, int amount, double value) {
    this.item = item;
    this.amount = amount;
    this.value = value;
  }

  @Override
  public int compareTo(RewardItem o) {
    return Double.compare(o.value, this.value);
  }
}
