package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.GainLevelQuest;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.quests.ReachLevelQuest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerLevelChangeListener implements Listener {

    @EventHandler
    public void onPlayerLevelChange(@NotNull PlayerLevelChangeEvent event) {

        QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(event.getPlayer());
        if (questPlayer == null) return;

        for (Quest quest : questPlayer.getQuests()) {
            // GainLevelQuest
            if (quest instanceof GainLevelQuest) {
                handleGainLevelQuest(questPlayer, event, (GainLevelQuest) quest);
            }

            // ReachLevelQuest
            if (quest instanceof ReachLevelQuest) {
                handleReachLevelQuest(questPlayer, event, (ReachLevelQuest) quest);
            }
        }
    }

    private void handleGainLevelQuest(QuestPlayer questPlayer, PlayerLevelChangeEvent event, GainLevelQuest quest) {
        int newLevel = event.getNewLevel();
        int oldLevel = event.getOldLevel();

        if (newLevel > oldLevel) {
            quest.progress(newLevel - oldLevel, questPlayer);
        }
    }

    private void handleReachLevelQuest(QuestPlayer questPlayer, PlayerLevelChangeEvent event, ReachLevelQuest quest) {
        int newLevel = event.getNewLevel();

        if (quest.isCompleted()) return;

        if (newLevel < quest.getGoal()) {
            quest.setCount(newLevel);
            quest.progress(0, questPlayer); // purpose: progress notification to player
        } else {
            quest.progress(quest.getGoal() - quest.getCount(), questPlayer);
        }
    }
}
