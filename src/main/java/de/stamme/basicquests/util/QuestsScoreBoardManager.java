package de.stamme.basicquests.util;

import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class QuestsScoreBoardManager {

	public static void show(QuestPlayer player) {
		
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		assert manager != null;

		Scoreboard board = manager.getNewScoreboard();
				
		// Show Scoreboard
		if (player.quests.size() > 0) {
			
			Objective score = board.registerNewObjective("quests", "criteria", "Quests");
			
			score.setDisplaySlot(DisplaySlot.SIDEBAR);
			
			for (Quest q: player.quests) {
				if (!q.completed()) {
					String name = (q.getName().length() > 32) ? (q.getName().substring(0, 29) + "...") :  q.getName();
					int value = q.goal - q.count;
					score.getScore(String.format(" %s> %s", ChatColor.GOLD, ChatColor.WHITE) + name).setScore(Math.max(value, 0));
					
				}
			}
		}
		
		
		player.player.setScoreboard(board);
		
	}
	
	public static void hide(QuestPlayer player) {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		assert manager != null;

		player.player.setScoreboard(manager.getNewScoreboard());
		Main.log("Scoreboard hidden");
	}
	
	public static void refresh(QuestPlayer player) {
		Scoreboard board = player.player.getScoreboard();
		
		// only if scoreboard is shown
		if (board.getObjectives().size() > 0) {
			show(player);
		}
	}
}
