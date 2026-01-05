package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.rewards.Reward;
import de.stamme.basicquests.util.StringFormatter;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import org.bukkit.Statistic;

public class IncreaseStatQuest extends Quest {

  // ---------------------------------------------------------------------------------------
  // Quest State
  // ---------------------------------------------------------------------------------------

  private final Statistic statistic;
  private final int startValue;

  // ---------------------------------------------------------------------------------------
  // Constructor
  // ---------------------------------------------------------------------------------------

  public IncreaseStatQuest(Statistic statistic, int startValue, int goal, Reward reward) {
    super(goal, reward);
    this.startValue = startValue;
    this.statistic = statistic;
  }

  // ---------------------------------------------------------------------------------------
  // Functionality
  // ---------------------------------------------------------------------------------------

  /** Periodically called to check for progress in quests with travel distance goal */
  public void checkForProgress(QuestPlayer questPlayer) {
    int questProgress;
    if (getStatistic() == Statistic.WALK_ONE_CM || getStatistic() == Statistic.SPRINT_ONE_CM) {
      questProgress =
          (questPlayer.getPlayer().getStatistic(Statistic.WALK_ONE_CM)
                  + questPlayer.getPlayer().getStatistic(Statistic.SPRINT_ONE_CM))
              - getStartValue();
    } else {
      questProgress = questPlayer.getPlayer().getStatistic(getStatistic()) - getStartValue();
    }

    if (questProgress == super.getCount()) {
      return;
    }

    // Progress has been made
    progress(questProgress - super.getCount(), questPlayer);
  }

  @Override
  public QuestData toData() {
    QuestData data = super.toData();
    data.setQuestType(QuestType.INCREASE_STAT.name());
    data.setStatistic(statistic.name());
    data.setStartValue(startValue);
    return data;
  }

  // ---------------------------------------------------------------------------------------
  // Getter & Setter
  // ---------------------------------------------------------------------------------------

  @Override
  public String getName() {
    switch (statistic) {
      case WALK_ONE_CM:
      case SPRINT_ONE_CM:
        return MessageFormat.format(
            MessagesConfig.getMessage("quests.increase-stat.run"), getDistanceGoal());
      case SWIM_ONE_CM:
        return MessageFormat.format(
            MessagesConfig.getMessage("quests.increase-stat.swim"), getDistanceGoal());
      case BOAT_ONE_CM:
        return MessageFormat.format(
            MessagesConfig.getMessage("quests.increase-stat.boat"), getDistanceGoal());
      case HORSE_ONE_CM:
        return MessageFormat.format(
            MessagesConfig.getMessage("quests.increase-stat.horse"), getDistanceGoal());
      case PIG_ONE_CM:
        return MessageFormat.format(
            MessagesConfig.getMessage("quests.increase-stat.pig"), getDistanceGoal());
      case STRIDER_ONE_CM:
        return MessageFormat.format(
            MessagesConfig.getMessage("quests.increase-stat.strider"), getDistanceGoal());
      case MINECART_ONE_CM:
        return MessageFormat.format(
            MessagesConfig.getMessage("quests.increase-stat.minecart"), getDistanceGoal());
      case RECORD_PLAYED:
        return MessagesConfig.getMessage("quests.increase-stat.music");
      case FLOWER_POTTED:
        return MessagesConfig.getMessage("quests.increase-stat.pot-plant");
      case RAID_WIN:
        return MessagesConfig.getMessage("quests.increase-stat.win-raid");
      case BELL_RING:
        return MessagesConfig.getMessage("quests.increase-stat.bell");
      case CAKE_SLICES_EATEN:
        return MessagesConfig.getMessage("quests.increase-stat.cake");
      default:
        return MessageFormat.format(
            MessagesConfig.getMessage("quests.increase-stat.generic"),
            StringFormatter.format(statistic.name()),
            getGoal());
    }
  }

  public String getDistanceGoal() {
    double distanceInMeters = getGoalInMeters();
    if (distanceInMeters < 1000) {
      NumberFormat format = new DecimalFormat("###.##m");
      return format.format(distanceInMeters);
    } else {
      NumberFormat format = new DecimalFormat("###.##km");
      return format.format(distanceInMeters / 1000);
    }
  }

  @Override
  public String[] getOptionNames() {
    return new String[] {QuestType.INCREASE_STAT.name(), statistic.name()};
  }

  @Override
  public String getOptionKey() {
    return statistic.name();
  }

  @Override
  public QuestType getQuestType() {
    return QuestType.INCREASE_STAT;
  }

  public Statistic getStatistic() {
    return statistic;
  }

  public int getStartValue() {
    return startValue;
  }

  public int getProgressInMeters() {
    return super.getCount() / 100;
  }

  public int getGoalInMeters() {
    return super.getGoal() / 100;
  }

  @Override
  public int getGoal() {
    switch (statistic) {
      case WALK_ONE_CM:
      case SPRINT_ONE_CM:
      case SWIM_ONE_CM:
      case BOAT_ONE_CM:
      case HORSE_ONE_CM:
      case PIG_ONE_CM:
      case STRIDER_ONE_CM:
      case MINECART_ONE_CM:
        return getGoalInMeters();
      default:
        return super.getGoal();
    }
  }

  @Override
  public int getCount() {
    switch (statistic) {
      case WALK_ONE_CM:
      case SPRINT_ONE_CM:
      case SWIM_ONE_CM:
      case BOAT_ONE_CM:
      case HORSE_ONE_CM:
      case PIG_ONE_CM:
      case STRIDER_ONE_CM:
      case MINECART_ONE_CM:
        return getProgressInMeters();
      default:
        return super.getCount();
    }
  }

  @Override
  public String getProgressString() {
    if (isCompleted()) {
      return super.getProgressString();
    }
    switch (statistic) {
      case WALK_ONE_CM:
      case SPRINT_ONE_CM:
      case SWIM_ONE_CM:
      case BOAT_ONE_CM:
      case HORSE_ONE_CM:
      case PIG_ONE_CM:
      case STRIDER_ONE_CM:
      case MINECART_ONE_CM:
        return getCount() + "/" + getGoal() + "m";
      default:
        return super.getProgressString();
    }
  }
}
