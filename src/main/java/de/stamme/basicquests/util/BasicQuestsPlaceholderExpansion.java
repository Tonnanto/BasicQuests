package de.stamme.basicquests.util;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.QuestPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class BasicQuestsPlaceholderExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public BasicQuestsPlaceholderExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "quests";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Tonanto";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String param) {

        Main.log("onRequest");
        QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer(player.getUniqueId());
        if (questPlayer == null) return null;

        // %quests%
        if (param.equalsIgnoreCase("")) {
            return questPlayer.getQuestsMessage();
        }

        // %quests_count%
        if (param.equalsIgnoreCase("count")) {
            return questPlayer.getQuests().size() + "";
        }

        // %quests_detail%
        if (param.equalsIgnoreCase("detail")) {
            return questPlayer.getQuestsWithRewardsMessage();
        }

        String[] params = param.split("_");

        // %quests_1..%
        try {
            int questIndex = Integer.parseInt(params[0]) - 1;

            if (params.length == 1) {
                // %quests_1%
                String[] questLines = questPlayer.getQuestsMessage().split("\n");
                if (questLines.length <= questIndex) return null;
                return questLines[questIndex];

            } else if (params[1].equalsIgnoreCase("title")) {
                // %quests_1_title%
                return questPlayer.getQuests().get(questIndex).getName();

            } else if (params[1].equalsIgnoreCase("progress")) {
                // %quests_1_progress%
                return questPlayer.getQuests().get(questIndex).getProgressString();

            } else if (params[1].equalsIgnoreCase("left")) {
                // %quests_1_left%
                return questPlayer.getQuests().get(questIndex).getLeftString();
            }

        } catch (NumberFormatException ignored) {}

        return null;
    }
}
