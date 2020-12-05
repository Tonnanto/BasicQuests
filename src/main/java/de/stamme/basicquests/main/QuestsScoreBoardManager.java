package de.stamme.basicquests.main;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import de.stamme.basicquests.quests.Quest;
import net.md_5.bungee.api.ChatColor;

public class QuestsScoreBoardManager {

	public static void show(QuestPlayer player) {
		
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
				
		// Show Scoreboard
		if (player.quests.size() > 0) {
			
			Objective score = board.registerNewObjective("quests", "criteria", "Quests");
			
			score.setDisplaySlot(DisplaySlot.SIDEBAR);
			
			for (Quest q: player.quests) {
				if (!q.completed()) {
					String name = (q.getName().length() > 32) ? (q.getName().substring(0, 29) + "...") :  q.getName();
					int value = q.goal - q.count;
					score.getScore(String.format(" %s> %s", ChatColor.GOLD, ChatColor.WHITE) + name).setScore((value >= 0) ? value : 0);
					
				}
			}
		}
		
		
		player.player.setScoreboard(board);
		
	}
	
	public static void hide(QuestPlayer player) {
		player.player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
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
