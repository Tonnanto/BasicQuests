package de.stamme.basicquests.model;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.quests.QuestData;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Used to serialize and persist a players data
 */
public class PlayerData implements Serializable {
	private static final long serialVersionUID = 9089937654326346356L;

    public List<QuestData> questSnapshot;
    public int skipCount;
    public int questsCompleted;
    public int starsGained;
    public int totalPoints;

    // 0 - no
	// 1 - yes
	// 2 - yes with rewards
    public int showScoreboard;

    public PlayerData(QuestPlayer questPlayer) {

		List<QuestData> questData = new ArrayList<>();
		if (questPlayer.getQuests() != null) {
			for (Quest q: questPlayer.getQuests()) {
				questData.add(q.toData());
			}
		}

    	this.skipCount = questPlayer.getSkipCount();
		this.questsCompleted = questPlayer.getQuestsCompleted();
        this.starsGained = questPlayer.getStarsGained();
        this.questSnapshot = questData;
        this.showScoreboard = questPlayer.getShowScoreboard();
    }

	/**
	 * Saves the PlayerData to a dedicated file
	 * @param filePath the path to save the data to
	 * @return whether the operation was successful
	 */
	public boolean saveData(String filePath) {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filePath)));
            out.writeObject(this);
            out.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

	public static PlayerData loadData(String filePath) {
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(filePath)));
            PlayerData data = (PlayerData) in.readObject();
            in.close();
            return data;
        } catch (ClassNotFoundException | IOException e) {
            return null;
        }
    }

	/**
	 * saves a players data to a dedicated file
	 * @param questPlayer the player to save the data from
	 * @return whether the operation was successful
	 */
	public static boolean getPlayerDataAndSave(QuestPlayer questPlayer) {
		PlayerData playerData = new PlayerData(questPlayer);
		 return playerData.saveData(filePathForUUID(questPlayer.getPlayer().getUniqueId()));
	}

	/**
	 * loads players quest from file when available. returns whether the operation was successful.
	 * @param player the player to load the data from
	 * @return whether the operation was successful
	 */
	public static boolean loadPlayerData(Player player) {

		String filepath = filePathForUUID(player.getUniqueId());
		if (!(new File(filepath)).exists()) {
			return false;
		}

        PlayerData data = PlayerData.loadData(filepath);

    	if (data != null) {
    		QuestPlayer questPlayer;
    		if (data.questSnapshot == null) { // failed to load quests
    			return false;
    		} else if (data.questSnapshot.size() == 0) {
				return false;
			}

    		questPlayer = new QuestPlayer(data, player);
    		if (data.showScoreboard >= 1) {
				QuestsScoreBoardManager.show(questPlayer, data.showScoreboard >= 2);
			}

    		BasicQuestsPlugin.getPlugin().getQuestPlayers().put(player.getUniqueId(), questPlayer);
    		BasicQuestsPlugin.log("PlayerData loaded: " + player.getName());

            return true;
    	} else
    		BasicQuestsPlugin.log(Level.SEVERE, "Could not fetch PlayerData. Creating new QuestPlayer.");

    	return false;
    }

	public static void resetSkipsForOfflinePlayer(OfflinePlayer player) {
		String filepath = filePathForUUID(player.getUniqueId());
		if (!(new File(filepath)).exists()) {
			return;
		}

        PlayerData data = PlayerData.loadData(filepath);
		if (data != null) {
			data.skipCount = 0;
			data.saveData(filepath);
		}
	}

	/**
	 * Deletes active quests for an OfflinePlayer
	 * This forces a regeneration (reset) once the player joins
	 * @param player OfflinePlayer
	 */
	public static void resetQuestsForOfflinePlayer(OfflinePlayer player) {
		String filepath = filePathForUUID(player.getUniqueId());
		if (!(new File(filepath)).exists()) {
			return;
		}

		PlayerData data = PlayerData.loadData(filepath);
		if (data != null) {
			// Deleting active quests will force a regeneration once the player joins
			data.questSnapshot = new ArrayList<>();
			data.saveData(filepath);
		}
	}

	public static String filePathForUUID(UUID id) {
		return BasicQuestsPlugin.getUserdataPath() + "/" + id + ".data";
	}

}
