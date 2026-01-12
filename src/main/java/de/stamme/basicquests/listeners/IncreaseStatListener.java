package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.IncreaseStatQuest;
import de.stamme.basicquests.model.quests.Quest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.jetbrains.annotations.NotNull;

public class IncreaseStatListener implements Listener {

    @EventHandler
    public void onStatisticIncrement(@NotNull PlayerStatisticIncrementEvent event) {
        if (event.isCancelled()) {
            return;
        }

        QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(event.getPlayer());
        if (questPlayer == null) {
            return;
        }

        for (Quest quest : questPlayer.getQuests()) {
            if ((quest instanceof IncreaseStatQuest)) {
                handleIncreaseStatQuest(questPlayer, event, (IncreaseStatQuest) quest);
            }
        }
    }

    private void handleIncreaseStatQuest(QuestPlayer questPlayer, PlayerStatisticIncrementEvent event, IncreaseStatQuest quest) {
        if (quest.getStatistic() == event.getStatistic()) {
            quest.progress(1, questPlayer);
        }
    }
}
