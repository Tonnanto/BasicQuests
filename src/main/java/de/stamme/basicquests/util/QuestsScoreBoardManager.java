package de.stamme.basicquests.util;

import de.stamme.basicquests.Config;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.*;

public class QuestsScoreBoardManager {

	public static void show(QuestPlayer questPlayer) {
		if (Config.isScoreboardDisabled()) return;
		
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		assert manager != null;

		Scoreboard board = manager.getNewScoreboard();
				
		// Show Scoreboard
		if (questPlayer.getQuests().size() > 0) {
			
			Objective score = board.registerNewObjective("quests", "criteria", "Quests");
			score.setDisplaySlot(DisplaySlot.SIDEBAR);

			for (Quest q: questPlayer.getQuests()) {
				if (q != null && !q.isCompleted()) {
					String name = (q.getName().length() > 32) ? (q.getName().substring(0, 29) + "...") :  q.getName();
					int value = q.getGoal() - q.getCount();
					score.getScore(String.format(" %s> %s", ChatColor.GOLD, ChatColor.WHITE) + name).setScore(Math.max(value, 0));
				}
			}
		}
		
		
		questPlayer.getPlayer().setScoreboard(board);
		
	}
	
	public static void hide(QuestPlayer questPlayer) {
		if (Config.isScoreboardDisabled()) return;

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		assert manager != null;

		questPlayer.getPlayer().setScoreboard(manager.getNewScoreboard());
	}
	
	public static void refresh(QuestPlayer questPlayer) {
		if (Config.isScoreboardDisabled()) return;

		Scoreboard board = questPlayer.getPlayer().getScoreboard();
		
		// only if scoreboard is shown
		if (board.getObjectives().size() > 0) {
			show(questPlayer);
		}
	}
}
