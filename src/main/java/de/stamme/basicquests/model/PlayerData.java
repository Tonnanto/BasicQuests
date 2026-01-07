package de.stamme.basicquests.model;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.quests.QuestData;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/** Used to serialize and persist a player's data */
public class PlayerData implements Serializable {
  private static final long serialVersionUID = 9089937654326346356L;

  public List<QuestData> questSnapshot;
  public int skipCount;

  // 0 - no
  // 1 - yes
  // 2 - yes with rewards
  public int showScoreboard;

  public PlayerData(QuestPlayer questPlayer) {

    List<QuestData> questData = new ArrayList<>();
    if (questPlayer.getQuests() != null) {
      for (Quest q : questPlayer.getQuests()) {
        questData.add(q.toData());
      }
    }

    this.skipCount = questPlayer.getSkipCount();
    this.questSnapshot = questData;
    this.showScoreboard = questPlayer.getShowScoreboard();
  }

  /**
   * Saves the PlayerData to a dedicated file
   *
   * @param filePath the path to save the data to
   * @return whether the operation was successful
   */
  public boolean saveData(String filePath) {
    try {
      BukkitObjectOutputStream out =
          new BukkitObjectOutputStream(
              new GZIPOutputStream(Files.newOutputStream(Paths.get(filePath))));
      out.writeObject(this);
      out.close();

      return true;
    } catch (IOException e) {
      BasicQuestsPlugin.log(Level.SEVERE, e.getMessage());
      return false;
    }
  }

  public static PlayerData loadData(String filePath) {
    try {
      BukkitObjectInputStream in =
          new BukkitObjectInputStream(
              new GZIPInputStream(Files.newInputStream(Paths.get(filePath))));
      PlayerData data = (PlayerData) in.readObject();
      in.close();
      return data;
    } catch (InvalidObjectException e) {
      Bukkit.getLogger().warning("Invalid player data: " + e.getMessage());
      return null;

    } catch (ClassNotFoundException | IOException e) {
      Bukkit.getLogger().severe("Failed to load player data");
      e.printStackTrace();
      return null;
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    if (questSnapshot == null) {
      throw new InvalidObjectException("questSnapshot must not be null");
    }

    for (QuestData quest : questSnapshot) {
      if (quest == null) {
        throw new InvalidObjectException("questSnapshot contains null QuestData");
      }
    }
  }

  /**
   * saves a players data to a dedicated file
   *
   * @param questPlayer the player to save the data from
   * @return whether the operation was successful
   */
  public static boolean getPlayerDataAndSave(QuestPlayer questPlayer) {
    PlayerData playerData = new PlayerData(questPlayer);
    return playerData.saveData(filePathForUUID(questPlayer.getPlayer().getUniqueId()));
  }

  /**
   * loads players quest from file when available. returns whether the operation was successful.
   *
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
      } else if (data.questSnapshot.isEmpty()) {
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
      BasicQuestsPlugin.log(
          Level.SEVERE,
          "Could not fetch PlayerData of " + player.getName() + ". Creating new QuestPlayer.");

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
   * Deletes active quests for an OfflinePlayer This forces a regeneration (reset) once the player
   * joins
   *
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
