package de.stamme.basicquests.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.entity.Player;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.quests.QuestData;

public class PlayerData implements Serializable {
	private static final long serialVersionUID = 9089937654326346356L;
		
    public final ArrayList<QuestData> questSnapshot;
    public int skipCount;
    
    // used for saving
    public PlayerData(QuestPlayer player) {
    	
		ArrayList<QuestData> questData = new ArrayList<QuestData>();
		
		if (player.quests != null) {
			for (Quest q: player.quests) {
				questData.add(q.toData());
			}
		}
		
    	this.skipCount = player.skipCount;
        this.questSnapshot = questData;
    }
    
//    // Can be used for loading
//    public PlayerData(PlayerData loadedData) {
//        this.questSnapshot = loadedData.questSnapshot;
//    }

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
		playerData.saveData(filePathForPlayer(player.player));
		Main.log("PlayerData Saved: " + player.getName());
	}
	
	// loads players quest from file when available. returns whether the operation was successful.
	public static boolean loadPlayerData(Player player) {
		
		String filepath = filePathForPlayer(player);
		if (!(new File(filepath)).exists()) {
			Main.log("No playerdata file found for: " + player.getName());
			return false;
		}
		
        PlayerData data = PlayerData.loadData(filepath);
        
        
    	if (data != null) {
    		
    		Main.log("PlayerData successfully fetched");
    		
    		QuestPlayer questPlayer;
    		if (data.questSnapshot == null | data.questSnapshot.size() == 0) { // failed to load quests
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
	
	public static String filePathForPlayer(Player player) {
		return Main.userdata_path + "/" + player.getUniqueId() + ".data";
	}
	
}
