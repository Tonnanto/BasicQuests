package de.stamme.basicquests.util.fastboard;

import de.stamme.basicquests.Config;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.util.L10n;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class QuestsScoreBoardManager {

	private static final Map<UUID, FastBoard> boards = new HashMap<>();

	public static FastBoard getBoardForPlayer(Player player) {
		if (boards.containsKey(player.getUniqueId())) {
			return boards.get(player.getUniqueId());
		}

		FastBoard board = new FastBoard(player);
		board.updateTitle(ChatColor.BOLD + L10n.getMessage("quest.plural"));
		boards.put(player.getUniqueId(), board);
		return board;
	}

	public static void show(QuestPlayer questPlayer, boolean withRewards) {
		if (Config.isScoreboardDisabled()) return;

		// Persist scoreboard setting
		if (withRewards) {
			questPlayer.setShowScoreboard(2);
		} else {
			questPlayer.setShowScoreboard(1);
		}

		FastBoard board = getBoardForPlayer(questPlayer.getPlayer());

		List<String> lines = new ArrayList<>();

		if (questPlayer.getQuests().size() > 0) {
			for (Quest q: questPlayer.getQuests()) {
				if (withRewards && !lines.isEmpty()) {
					lines.add("\n");
				}
				if (q != null) {
					String questString = ChatColor.GOLD + "> " + q.getInfo(false);
					if (withRewards) questString += q.getReward();
					lines.addAll(Arrays.stream(questString.split("\n")).collect(Collectors.toList()));
				}
			}
		}

		if (lines.size() > 15) {
			int linesMore = lines.size() - 14;
			lines = lines.subList(0, 14);
			lines.add(ChatColor.GRAY + "... " + linesMore + " more lines");
		}

		board.updateLines(lines);
	}
	
	public static void hide(QuestPlayer questPlayer) {
		if (Config.isScoreboardDisabled()) return;

		// Persist scoreboard setting
		questPlayer.setShowScoreboard(0);

		FastBoard board = getBoardForPlayer(questPlayer.getPlayer());
		boards.remove(questPlayer.getPlayer().getUniqueId());
		board.delete();
	}
	
	public static void refresh(QuestPlayer questPlayer) {
		if (Config.isScoreboardDisabled()) return;
		show(questPlayer, questPlayer.getShowScoreboard() >= 2);
	}

	public static boolean isBoardShowing(Player player) {
		return boards.containsKey(player.getUniqueId());
	}
}