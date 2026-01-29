package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.MessagesConfig;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class ReloadCommand extends BasicQuestsCommand {
    protected ReloadCommand() {
        super("reload");
    }

    @Override
    public final @NotNull String getPermission() {
        return "basicquests.admin.reload";
    }

    @Override
    public void evaluate(@NotNull BasicQuestsPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        plugin.reload();
        BasicQuestsPlugin.sendMessage(sender, MessagesConfig.getMessage("commands.reload.success"));
    }
}
