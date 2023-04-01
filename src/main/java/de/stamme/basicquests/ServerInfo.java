package de.stamme.basicquests;

import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.quests.QuestData;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ServerInfo implements Serializable {

    // Singleton
    private static transient ServerInfo instance;

    private ServerInfo() {
        completedQuests = new HashMap<>();
        skippedQuests = new HashMap<>();
        questsLeaderboard = new HashMap<>();
    }

    public static ServerInfo getInstance() {
        if (instance != null) return instance;
        ServerInfo loadedInfo = load();
        if (loadedInfo != null) instance = loadedInfo;
        else instance = new ServerInfo();
        return instance;
    }

    // Attributes
    private static final String path = BasicQuestsPlugin.getPlugin().getDataFolder() +  "/server_info.data";

    private final HashMap<QuestData, LocalDateTime> completedQuests;
    private final HashMap<QuestData, LocalDateTime> skippedQuests;
    private long totalQuestCount;
    private long totalSkipCount;
    private LocalDateTime lastSkipReset;
    private final HashMap<UUID, Integer> questsLeaderboard;

    public static void save() {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(path)));
            out.writeObject(getInstance());
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ServerInfo load() {
        Object obj = null;

        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(path)));
            obj = in.readObject();
        } catch (Exception ignored) {}

        if (obj instanceof ServerInfo)
            return (ServerInfo) obj;
        else return null;
    }

    private void cleanMap(HashMap<QuestData, LocalDateTime> map) {
        for (Map.Entry<QuestData, LocalDateTime> entry: map.entrySet()) {
            long secondsAgo = Duration.between(entry.getValue(), LocalDateTime.now()).getSeconds();
            if (secondsAgo > 604800) // 1 Week
                map.remove(entry.getKey());
        }
    }

    // Setter
    public void questCompleted(Quest quest, QuestPlayer player) {
        QuestData questData = quest.toData();
        totalQuestCount++;
        completedQuests.put(questData, LocalDateTime.now());

        // Update leaderboard
        questsLeaderboard.put(player.getPlayer().getUniqueId(), player.getQuestsCompleted());
    }

    public void questSkipped(Quest quest) {
        QuestData questData = quest.toData();
        totalSkipCount++;
        skippedQuests.put(questData, LocalDateTime.now());
    }

    public void setLastSkipReset(LocalDateTime t) {
        lastSkipReset = t;
    }

    // Getter
    public LocalDateTime getLastSkipReset() {
        return lastSkipReset;
    }

    public long getTotalQuestCount() {
        return totalQuestCount;
    }

    public long getTotalSkipCount() {
        return totalSkipCount;
    }

    public HashMap<QuestData, LocalDateTime> getCompletedQuests() {
        cleanMap(completedQuests);
        return completedQuests;
    }

    public HashMap<QuestData, LocalDateTime> getSkippedQuests() {
        cleanMap(skippedQuests);
        return skippedQuests;
    }

    public List<Map.Entry<UUID, Integer>> getQuestsLeaderboard() {
        return questsLeaderboard.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toList());
    }

}
