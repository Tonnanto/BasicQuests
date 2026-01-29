package de.stamme.basicquests.util;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.config.Config;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final int resourceId;

    @Nullable
    private String newVersion;

    private final String currentVersion;

    private static UpdateChecker instance;

    public static UpdateChecker getInstance() {
        if (instance == null) {
            instance = new UpdateChecker(BasicQuestsPlugin.getPlugin(), BasicQuestsPlugin.getSpigotMCID());
        }
        return instance;
    }

    private UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.currentVersion = plugin.getDescription().getVersion();
        register();
    }

    public void register() {
        if (!Config.checkForUpdates()) {
            return;
        }

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            BasicQuestsPlugin.log("Checking for Updates ... ");
            getVersion(version -> {
                String oldVersion = plugin.getDescription().getVersion();
                if (oldVersion.equalsIgnoreCase(version)) {
                    BasicQuestsPlugin.log("No Update available.");
                } else {
                    notifyUser(plugin.getServer().getConsoleSender());
                    for (CommandSender player : plugin.getServer().getOnlinePlayers()) {
                        if (player.hasPermission("basicquests.admin.update")) {
                            notifyUser(player);
                        }
                    }
                }
            });
        }, 0, 432000);
    }

    /**
     * Pulls the most recent version of BasicQuests from SpigotMC
     *
     * @param consumer the consumer to accept the pulled version string
     */
    private void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream();
                    Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    String version = scanner.next();
                    newVersion = version;
                    consumer.accept(version);
                }
            } catch (IOException exception) {
                BasicQuestsPlugin.log("Cannot look for updates: " + exception.getMessage());
            }
        });
    }

    /**
     * Notifies a CommandSender of the new version
     *
     * @param user CommandSender
     */
    public void notifyUser(CommandSender user) {
        if (!Config.checkForUpdates()) {
            return;
        }
        if (newVersion == null || newVersion.equals(currentVersion)) {
            return;
        }

        user.sendMessage(ChatColor.GREEN + String.format("Version %s of BasicQuests is now available:", newVersion));
        user.sendMessage(ChatColor.DARK_GREEN + plugin.getDescription().getWebsite());
        user.sendMessage(ChatColor.GREEN + String.format("Your version: %s", currentVersion));
    }
}
