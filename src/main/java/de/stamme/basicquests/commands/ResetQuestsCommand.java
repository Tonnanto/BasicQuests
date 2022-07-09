package de.stamme.basicquests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class ResetQuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

		// Check for permission
		if (!sender.hasPermission("quests.reset")) {
			sender.sendMessage(String.format("%sYou are not allowed to use this command.", ChatColor.RED));
			return true;
		}

		if (args.length > 0) {
			if (args.length > 1) return false;
			// "/resetquests <player>"
			resetForOtherPlayer(sender, args[0]);
			return true;

		} else if (sender instanceof Player) {
			// "/resetquests"
			QuestPlayer questPlayer = Main.getPlugin().getQuestPlayer((Player) sender);
			if (questPlayer == null) {
				String errorMessage = String.format("%sFailed to locate QuestPlayer instance - Server reload recommended", ChatColor.RED);
				Main.log(errorMessage);
				sender.sendMessage(errorMessage);
				return true;
			}
			resetForSelf(questPlayer);
		}
		
		return true;
	}

	/**
	 * Tries to reset the quests of another player
	 * @param sender the sender of the command
	 * @param targetName the name of the player whos quests to reset
	 */
	void resetForOtherPlayer(@NotNull CommandSender sender, String targetName) {
		Player targetPlayer = Main.getPlugin().getServer().getPlayer(targetName);

		if (targetPlayer != sender && !sender.hasPermission("quests.reset.forothers")) {
			sender.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
			return;
		}

		QuestPlayer target = Main.getPlugin().getQuestPlayer(targetPlayer);
		if (target == null) {
			sender.sendMessage(String.format("%sPlayer %s was not found or is not online.", ChatColor.RED, targetName));
			return;
		}

		target.resetQuests();
		target.getPlayer().sendMessage(String.format("%sYour quests have been reset.", ChatColor.GREEN));
	}

	/**
	 * Tries to reset the quests for the player himself
	 * @param questPlayer the player who wants to reset his quests
	 */
	void resetForSelf(QuestPlayer questPlayer) {
		questPlayer.resetQuests();
		questPlayer.sendMessage(String.format("%sYour quests have been reset.", ChatColor.GREEN));
	}
}
