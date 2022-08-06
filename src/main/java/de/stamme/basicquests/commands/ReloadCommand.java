package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class ReloadCommand extends BasicQuestsCommand {

    protected ReloadCommand() {
        super("reload");
    }

    @Override
    public void evaluate(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "BasicQuests reloaded.");
    }
}
