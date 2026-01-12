package de.stamme.basicquests.model.rewards;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.util.StringFormatter;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Reward implements Serializable {
    private static final long serialVersionUID = 1970784300296164425L;

    private final BigDecimal money;
    private final int xp;
    private final List<RewardItem> rewardItems;
    private final List<String> materialNames;
    private final RewardType rewardType;

    // ---------------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------------

    /** Empty Reward */
    public Reward() {
        this.rewardType = null;
        this.xp = 0;
        this.rewardItems = new ArrayList<>();
        this.money = BigDecimal.ZERO;
        this.materialNames = null;
    }

    /** Money Reward */
    public Reward(BigDecimal money) {
        this.rewardType = RewardType.MONEY;
        this.xp = 0;
        this.money = money;
        this.rewardItems = new ArrayList<>();
        this.materialNames = null;
    }

    /** Item Reward */
    public Reward(List<RewardItem> items, List<String> materialNames) {
        this.rewardType = RewardType.ITEM;
        this.xp = 0;
        this.rewardItems = items;
        this.money = BigDecimal.ZERO;
        this.materialNames = materialNames;
    }

    /** XP Reward */
    public Reward(int xp) {
        this.rewardType = RewardType.XP;
        this.xp = xp;
        this.rewardItems = new ArrayList<>();
        this.money = BigDecimal.ZERO;
        this.materialNames = null;
    }

    // ---------------------------------------------------------------------------------------
    // Serialization
    // ---------------------------------------------------------------------------------------

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        if (rewardType == null) {
            throw new InvalidObjectException("rewardType must not be null");
        }

        if (rewardItems != null) {
            for (RewardItem item : rewardItems) {
                if (item == null) {
                    throw new InvalidObjectException("rewardItems contains null");
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Getter & Setter
    // ---------------------------------------------------------------------------------------

    public String moneyString(boolean debug) {
        String s = "";
        if (getMoney().compareTo(BigDecimal.ZERO) > 0) {
            s += MessageFormat.format(debug ? "\n + {0}" : MessagesConfig.getMessage("quest.rewards.format"),
                    BasicQuestsPlugin.getEconomy().format(getMoney().doubleValue()));
        }
        return s;
    }

    public String xpString(boolean debug) {
        String s = "";
        if (getXp() > 0) {
            s += MessageFormat.format(debug ? "\n + {0}" : MessagesConfig.getMessage("quest.rewards.format"), getXp() + " XP");
        }
        return s;
    }

    public String itemString(boolean debug) {
        StringBuilder sb = new StringBuilder("  ");

        if (!getRewardItems().isEmpty()) {
            getRewardItems().forEach(rewardItem -> sb.append(MessageFormat.format(debug ? "\n + {0}" : MessagesConfig.getMessage("quest.rewards.format"),
                    StringFormatter.formatItemStack(rewardItem.item, rewardItem.amount))));
        }

        return sb.toString();
    }

    public String toString() {
        StringBuilder s = new StringBuilder();

        if (getMoney().compareTo(BigDecimal.ZERO) > 0) {
            s.append(moneyString(false));
        } else if (getXp() > 0) {
            s.append(xpString(false));
        }

        s.append(itemString(false));

        return s.toString();
    }

    public String debugString() {
        StringBuilder s = new StringBuilder();

        if (getMoney().compareTo(BigDecimal.ZERO) > 0) {
            s.append(moneyString(true));
        } else if (getXp() > 0) {
            s.append(xpString(true));
        }

        s.append(itemString(true));

        return s.toString();
    }

    public BigDecimal getMoney() {
        return money;
    }

    public int getXp() {
        return xp;
    }

    public List<RewardItem> getRewardItems() {
        return rewardItems;
    }

    public List<String> getMaterialNames() {
        return materialNames;
    }

    public RewardType getRewardType() {
        return rewardType;
    }
}
