package de.stamme.basicquests.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.util.QuestsScoreBoardManager;
import org.jetbrains.annotations.NotNull;

public class HideQuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (!(sender instanceof  Player)) return false;

		QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer((Player) sender);
		if (questPlayer == null) return true;

		QuestsScoreBoardManager.hide(questPlayer);

		return true;
	}
}
