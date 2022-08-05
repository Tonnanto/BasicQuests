package de.stamme.basicquests.util;

import de.stamme.basicquests.Config;
import de.stamme.basicquests.Main;
import de.stamme.basicquests.ServerInfo;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.quests.QuestData;
import org.bstats.bukkit.Metrics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsService {
    public static final int pluginId = 9974;

    public static void setUpMetrics() {
        Metrics metrics = new Metrics(Main.getPlugin(), pluginId);


        // Economy Pie Chart
        metrics.addCustomChart(new Metrics.SimplePie("economy", () -> (Main.getEconomy() != null) ? "true" : "false"));

        // RewardType Pie Chart
        metrics.addCustomChart(new Metrics.SimplePie("reward_type", () -> {
            List<String> list = new ArrayList<>();

            if (Main.getEconomy() != null && Config.moneyRewards())
                list.add("Money");
            if (Config.itemRewards())
                list.add("Items");
            if (Config.xpRewards())
                list.add("XP");

            return String.join(", ", list);
        }));

        // quests completed line chart
        metrics.addCustomChart(new Metrics.SingleLineChart("quests_completed", () -> ServerInfo.getInstance().getCompletedQuests().size()));

        // quests skipped line chart
        metrics.addCustomChart(new Metrics.SingleLineChart("quests_skipped", () -> ServerInfo.getInstance().getSkippedQuests().size()));

        // quest type completed advanced pie chart
        metrics.addCustomChart(new Metrics.DrilldownPie("type_of_completed_quests_drilldown", () -> {
            Map<String, Map<String, Integer>> valueMap = new HashMap<>();
            for (QuestData data: ServerInfo.getInstance().getCompletedQuests().keySet()) {
                String questTypeName = StringFormatter.format(data.getQuestType());

                if (!valueMap.containsKey(questTypeName)) {
                    valueMap.put(questTypeName, new HashMap<>());
                }

                String optionName = "";
                Quest quest = data.toQuest();
                if (quest != null) {
                    optionName = StringFormatter.format(quest.getOptionKey());
                }

                valueMap.get(questTypeName).merge(optionName, 1, Integer::sum);
            }
            return valueMap;
        }));

        // quest type skipped advanced pie chart
        metrics.addCustomChart(new Metrics.DrilldownPie("type_of_skipped_quests_drilldown", () -> {
            Map<String, Map<String, Integer>> valueMap = new HashMap<>();
            for (QuestData data: ServerInfo.getInstance().getSkippedQuests().keySet()) {
                String questTypeName = StringFormatter.format(data.getQuestType());

                if (!valueMap.containsKey(questTypeName)) {
                    valueMap.put(questTypeName, new HashMap<>());
                }

                String optionName = "";
                Quest quest = data.toQuest();
                if (quest != null) {
                    optionName = StringFormatter.format(quest.getOptionKey());
                }

                valueMap.get(questTypeName).merge(optionName, 1, Integer::sum);
            }
            return valueMap;
        }));

        DecimalFormat df = new DecimalFormat("#.##");

        // quest amount pie chart
        metrics.addCustomChart(new Metrics.SimplePie("quest_amount", () -> String.valueOf(Config.getQuestAmount())));

        // reward-factor pie chart
        metrics.addCustomChart(new Metrics.SimplePie("reward_factor", () -> df.format(Config.getRewardFactor())));

        // quantity-factor pie chart
        metrics.addCustomChart(new Metrics.SimplePie("quantity_factor", () -> df.format(Config.getQuantityFactor())));

        // skips per day pie chart
        metrics.addCustomChart(new Metrics.SimplePie("skips_per_day", () -> df.format(Config.getSkipsPerDay())));


        // limit progress notifications pie chart
        metrics.addCustomChart(new Metrics.SimplePie("limit_progress_messages", () -> String.valueOf(Config.limitProgressMessages())));

        // broadcast-on-quest-complete pie chart
        metrics.addCustomChart(new Metrics.SimplePie("broadcast_on_complete", () -> String.valueOf(Config.broadcastOnQuestCompletion())));

        // sound-on-quest-complete pie chart
        metrics.addCustomChart(new Metrics.SimplePie("sound_on_complete", () -> String.valueOf(Config.soundOnQuestCompletion())));


        // locale pie chart
        metrics.addCustomChart(new Metrics.SimplePie("locale", Config::getLocale));


        // increase quantities by playtime pie chart
        metrics.addCustomChart(new Metrics.SimplePie("increase_quantities_with_playtime", () -> String.valueOf(Config.increaseAmountByPlaytime())));

        // playtime factor pie chart
        metrics.addCustomChart(new Metrics.SimplePie("playtime_factor", () -> df.format(Config.minPlaytimeFactor()) + " -> " + df.format(Config.maxPlaytimeFactor()) + " (at " + df.format(Config.maxPlaytimeHours()) + " hours)"));

        // duplicate quest chance pie chart
        metrics.addCustomChart(new Metrics.SimplePie("duplicate_quest_chance", () -> df.format(Config.duplicateQuestChance())));

        // disable scoreboard pie chart
        metrics.addCustomChart(new Metrics.SimplePie("disable_scoreboard", () -> String.valueOf(Config.isScoreboardDisabled())));

        // show-scoreboard-per-default pie chart
        metrics.addCustomChart(new Metrics.SimplePie("show_scoreboard_per_default", () -> String.valueOf(Config.showScoreboardPerDefault())));

    }
}
