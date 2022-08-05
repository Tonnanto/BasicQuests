package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.util.L10n;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.stamme.basicquests.model.QuestPlayer;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

public class ResetQuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

		// Check for permission
		if (!sender.hasPermission("quests.reset")) {
			sender.sendMessage(ChatColor.RED + L10n.getMessage("commands.commandNotAllowed"));
			return true;
		}

		if (args.length > 0) {
			if (args.length > 1) return false;
			// "/resetquests <player>"
			resetForOtherPlayer(sender, args[0]);
			return true;

		} else if (sender instanceof Player) {
			// "/resetquests"
			QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer((Player) sender);
			if (questPlayer == null) {
				String errorMessage = L10n.getMessage("commands.questPlayerNotFound");
				BasicQuestsPlugin.log(errorMessage);
				sender.sendMessage(ChatColor.RED + errorMessage);
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
		Player targetPlayer = BasicQuestsPlugin.getPlugin().getServer().getPlayer(targetName);

		if (targetPlayer != sender && !sender.hasPermission("quests.reset.forothers")) {
			sender.sendMessage(ChatColor.RED + L10n.getMessage("commands.actionNotAllowed"));
			return;
		}

		QuestPlayer target = BasicQuestsPlugin.getPlugin().getQuestPlayer(targetPlayer);
		if (target == null) {
			sender.sendMessage(ChatColor.RED + MessageFormat.format(L10n.getMessage("commands.playerNotFound"), targetName));
			return;
		}

		target.resetQuests();
		target.getPlayer().sendMessage(ChatColor.GREEN + L10n.getMessage("quests.questsHaveBeenReset"));
	}

	/**
	 * Tries to reset the quests for the player himself
	 * @param questPlayer the player who wants to reset his quests
	 */
	void resetForSelf(QuestPlayer questPlayer) {
		questPlayer.resetQuests();
		questPlayer.sendMessage(ChatColor.GREEN + L10n.getMessage("quests.questsHaveBeenReset"));
	}
}
