package de.stamme.basicquests.main;

import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.quests.QuestData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PlayerData implements Serializable {
	private static final long serialVersionUID = 9089937654326346356L;
		
    public final ArrayList<QuestData> questSnapshot;
    public int skipCount;
    
    // used for saving
    public PlayerData(QuestPlayer player) {
    	
		ArrayList<QuestData> questData = new ArrayList<>();
		
		if (player.quests != null) {
			for (Quest q: player.quests) {
				questData.add(q.toData());
			}
		}
		
    	this.skipCount = player.skipCount;
        this.questSnapshot = questData;
    }

    // Saves the PlayerData to a dedicated file
	public boolean saveData(String filePath) {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filePath)));
            out.writeObject(this);
            out.close();
            
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }	
	
	// saves a players data to a dedicated file
	public static void getPlayerDataAndSave(QuestPlayer player) {
		PlayerData playerData = new PlayerData(player);

		 if (playerData.saveData(filePathForUUID(player.player.getUniqueId()))) {
			 Main.log("PlayerData Saved: " + player.getName());
		 } else {
			 Main.log("Failed to save PlayerData: " + player.getName());
		 }
	}
	
	// loads players quest from file when available. returns whether the operation was successful.
	public static boolean loadPlayerData(Player player) {
		
		String filepath = filePathForUUID(player.getUniqueId());
		if (!(new File(filepath)).exists()) {
			Main.log("No PlayerData file found for: " + player.getName());
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
    		Main.log("Could not fetch PlayerData");

    	
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
