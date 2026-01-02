package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.events.QuestCompletedEvent;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.util.StringFormatter;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discord.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * Listens for events that should trigger a message to discord if EssentialsXDiscord is connected
 */
public class EssentialsDiscordHookListener implements Listener {

    @Nullable
    private final DiscordService discordService;
    private final MessageType type;

    public EssentialsDiscordHookListener(final Plugin plugin) {
        discordService = Bukkit.getServicesManager().load(DiscordService.class);
        type = new MessageType("quest-completed");

        if (discordService != null) {
            discordService.registerMessageType(plugin, type);
        }
    }

    @EventHandler
    public void onQuestCompleted(QuestCompletedEvent event) {
        if (!BasicQuestsPlugin.usingEssentialsDiscord) return;
        sendDiscordMessage(event.getQuest(), event.getPlayer());
    }

    private void sendDiscordMessage(Quest quest, QuestPlayer questPlayer) {
        if (discordService == null) return;

        String message = MessageFormat.format(
            MessagesConfig.getMessage("events.broadcast.quest-complete-discord"),
            questPlayer.getPlayer().getName(),
            quest.getName(),
            StringFormatter.starString(quest.getStarValue(), true)
        );

        final boolean allowGroupMentions = false;
        discordService.sendMessage(type, message, allowGroupMentions);
    }
}
