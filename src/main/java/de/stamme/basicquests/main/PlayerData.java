package de.stamme.basicquests.main;

import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.quests.QuestData;
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
		
    public final List<QuestData> questSnapshot;
    public int skipCount;

    public PlayerData(QuestPlayer questPlayer) {
    	
		List<QuestData> questData = new ArrayList<>();
		
		if (questPlayer.getQuests() != null) {
			for (Quest q: questPlayer.getQuests()) {
				questData.add(q.toData());
			}
		}
		
    	this.skipCount = questPlayer.getSkipCount();
        this.questSnapshot = questData;
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
            e.printStackTrace();
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
    		
    		Main.plugin.questPlayer.put(player.getUniqueId(), questPlayer);
    		Main.log("PlayerData loaded: " + player.getName());
    		
            return true;
    	} else
    		Main.log(Level.SEVERE, "Could not fetch PlayerData");

    	return false;
    }
	
	public static void resetSkipsForOfflinePlayer(OfflinePlayer player) {
		String filepath = filePathForUUID(player.getUniqueId());
		if (!(new File(filepath)).exists()) {
			Main.log("No PlayerData file found for: " + player.getName());
			return;
		}
		
        PlayerData data = PlayerData.loadData(filepath);
		if (data != null) {
			data.skipCount = 0;
			data.saveData(filepath);
		}
	}
	
	public static String filePathForUUID(UUID id) {
		return Main.userdata_path + "/" + id + ".data";
	}
	
}
