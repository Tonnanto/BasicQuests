package de.stamme.basicquests.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.MessagesConfig;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class BasicQuestsCommandRouter implements CommandExecutor, TabCompleter {

    @Unmodifiable
    private static final List<BasicQuestsCommand> COMMANDS = ImmutableList.of(new HelpCommand(), new ListCommand(), new ReloadCommand(), new ResetCommand(),
            new CompleteCommand(), new SkipCommand(), new RewardCommand(), new ScoreboardCommand(), new LeaderboardCommand());

    @NotNull
    private final BasicQuestsPlugin plugin;

    @NotNull
    @Unmodifiable
    private final Map<String, BasicQuestsCommand> commands;

    public BasicQuestsCommandRouter(@NotNull final BasicQuestsPlugin plugin) {
        this.plugin = plugin;

        final ImmutableMap.Builder<String, BasicQuestsCommand> commands = ImmutableMap.builder();

        for (final BasicQuestsCommand command : COMMANDS) {
            command.getLabels().forEach(label -> commands.put(label, command));
        }

        this.commands = commands.build();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, @NotNull final String[] args) {
        if (args.length == 0) {
            final BasicQuestsCommand fallback = commands.get("list");
            if (fallback != null) {
                fallback.evaluate(plugin, sender, "", Collections.emptyList());
            }

            return true;
        }

        final String search = args[0].toLowerCase(Locale.ROOT);
        final BasicQuestsCommand target = commands.get(search);

        if (target == null) {
            BasicQuestsPlugin.sendMessage(sender, MessageFormat.format(MessagesConfig.getMessage("generic.unknown-command"), search));
            return true;
        }

        final String permission = target.getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            BasicQuestsPlugin.sendMessage(sender, MessagesConfig.getMessage("generic.no-permission"));
            return true;
        }

        target.evaluate(plugin, sender, search, Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias,
            @NotNull final String[] args) {
        final List<String> suggestions = new ArrayList<>();

        if (args.length > 1) {
            final BasicQuestsCommand target = this.commands.get(args[0].toLowerCase(Locale.ROOT));

            if (target != null) {
                target.complete(plugin, sender, args[0].toLowerCase(Locale.ROOT), Arrays.asList(Arrays.copyOfRange(args, 1, args.length)), suggestions);
            }

            return suggestions;
        }

        final Stream<String> targets = BasicQuestsCommand.filterByPermission(sender, commands.values().stream()).map(BasicQuestsCommand::getLabels)
                .flatMap(Collection::stream);
        BasicQuestsCommand.suggestByParameter(targets, suggestions, args.length == 0 ? null : args[0]);

        return suggestions;
    }
}
