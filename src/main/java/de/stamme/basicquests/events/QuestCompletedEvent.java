package de.stamme.basicquests.events;

import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QuestCompletedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected Quest quest;
    protected QuestPlayer questPlayer;

    public QuestCompletedEvent(final Quest quest, final QuestPlayer player) {
        this.quest = quest;
        this.questPlayer = player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets the quest involved in this event.
     *
     * @return The Quest which is involved in this event
     */
    public final Quest getQuest() {
        return quest;
    }

    /**
     * Gets the quest player involved in this event.
     *
     * @return The QuestPlayer who is involved in this event
     */
    public final QuestPlayer getPlayer() {
        return questPlayer;
    }
}
