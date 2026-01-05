package de.stamme.basicquests.model.rewards;

import de.stamme.basicquests.config.Config;

public enum RewardType {
  ITEM,
  MONEY,
  XP;

  public double getWeight() {
    switch (this) {
      case ITEM:
        return Config.getItemRewardsWeight();
      case MONEY:
        return Config.getMoneyRewardsWeight();
      case XP:
        return Config.getXpRewardsWeight();
    }
    return 1.0;
  }
}
