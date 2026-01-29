package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.EntityKillQuest;
import de.stamme.basicquests.model.quests.Quest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

public class EntityDeathListener implements Listener {

    @EventHandler
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.getKiller() == null) {
            return;
        }
        if (!BasicQuestsPlugin.getPlugin().getQuestPlayers().containsKey(entity.getKiller().getUniqueId())) {
            return;
        }

        QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(entity.getKiller());
        if (questPlayer == null) {
            return;
        }

        for (Quest quest : questPlayer.getQuests()) {
            if (!(quest instanceof EntityKillQuest)) {
                continue;
            } // is EntityKillQuest
            EntityKillQuest ekq = (EntityKillQuest) quest;

            if (ekq.getEntity() != entity.getType()) {
                continue;
            } // is correct entity
            ekq.progress(1, questPlayer);
        }
    }
}
