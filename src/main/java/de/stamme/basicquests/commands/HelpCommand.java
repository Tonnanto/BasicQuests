package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.config.MessagesConfig;
import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public class HelpCommand extends BasicQuestsCommand {

  protected HelpCommand() {
    super("help");
  }

  @Override
  public @Nullable String getPermission() {
    return "basicquests.use.help";
  }

  @Override
  public void evaluate(
      @NotNull BasicQuestsPlugin plugin,
      @NotNull CommandSender sender,
      @NotNull String alias,
      @NotNull @Unmodifiable List<String> params) {

    StringBuilder sb = new StringBuilder();

    // Title - Header
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.header"));

    // General information about the plugin
    ChoiceFormat questAmountFormat =
        new ChoiceFormat(
            new double[] {1, 2},
            new String[] {
              MessagesConfig.getMessage("generic.quest.singular"),
              MessagesConfig.getMessage("generic.quest.plural"),
            });
    sb.append("\n")
        .append(
            MessageFormat.format(
                MessagesConfig.getMessage("commands.help.info"),
                String.format(
                    "%s %s",
                    Config.getQuestAmount(), questAmountFormat.format(Config.getQuestAmount())),
                String.format(
                    "%s %s",
                    Config.getSkipsPerDay(), questAmountFormat.format(Config.getSkipsPerDay()))));

    // Player Commands
    sb.append("\n");
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.list-command"));
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.list-info"));
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.reward-command"));
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.reward-info"));
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.skip-command"));
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.skip-info"));
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.scoreboard-command"));
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.scoreboard-info"));
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.leaderboard-command"));
    sb.append("\n").append(MessagesConfig.getMessage("commands.help.leaderboard-info"));

    // Admin Commands
    sb.append("\n");
    if (sender.hasPermission("basicquests.admin.list")) {
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.list-other-command"));
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.list-other-info"));
    }
    if (sender.hasPermission("basicquests.admin.skip.others")) {
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.skip-other-command"));
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.skip-other-info"));
    }
    if (sender.hasPermission("basicquests.admin.complete")) {
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.complete-command"));
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.complete-info"));
    }
    if (sender.hasPermission("basicquests.admin.complete.others")) {
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.complete-other-command"));
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.complete-other-info"));
    }
    if (sender.hasPermission("basicquests.admin.reset")) {
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.reset-command"));
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.reset-info"));
    }
    if (sender.hasPermission("basicquests.admin.reset.others")) {
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.reset-other-command"));
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.reset-other-info"));
    }
    if (sender.hasPermission("basicquests.admin.reload")) {
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.reload-command"));
      sb.append("\n").append(MessagesConfig.getMessage("commands.help.reload-info"));
      sb.append("\n");
    }

    BasicQuestsPlugin.sendRawMessage(sender, sb.toString());
  }
}
