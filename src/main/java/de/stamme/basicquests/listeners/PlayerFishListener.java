package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.FishItemQuest;
import de.stamme.basicquests.model.quests.Quest;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerFishListener implements Listener {

    @EventHandler
    public void onPlayerFish(@NotNull PlayerFishEvent event) {
        if (event.isCancelled()) return;

        QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(event.getPlayer());
        if (questPlayer == null) return;

        for (Quest quest : questPlayer.getQuests()) {
            if (quest instanceof FishItemQuest) {
                handleFishItemQuest(questPlayer, event, (FishItemQuest) quest);
            }
        }
    }

    private void handleFishItemQuest(QuestPlayer questPlayer, PlayerFishEvent event, FishItemQuest quest) {

        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        if (!(event.getCaught() instanceof Item)) return;

        if (quest.itemMatches((Item) event.getCaught())) {
            quest.progress(1, questPlayer);
        }
    }

}
