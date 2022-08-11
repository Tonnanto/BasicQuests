package de.stamme.basicquests.config;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.quests.QuestType;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.spigot.SpigotSerializer;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.logging.Level;

public class MessagesConfig {
    private static YamlDocument messages;

    /**
     * Register the messages configuration.
     */
    public static void register(String locale) {
        BasicQuestsPlugin plugin = BasicQuestsPlugin.getPlugin();
        String filePath = "lang/messages_" + locale + ".yml";
        InputStream resource = plugin.getResource(filePath);

        if (resource == null) {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not find messages file for locale \"" + locale + "\". Using \"en\" instead.");
            filePath = "lang/messages_en.yml";
            resource = plugin.getResource(filePath);
        }

        assert resource != null;

        try {
            messages = YamlDocument.create(
                new File(plugin.getDataFolder(), filePath),
                resource,
                GeneralSettings.builder().setSerializer(SpigotSerializer.getInstance()).build(),
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build()
            );
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrieve the localized message.
     *
     * @param  key The message key.
     * @return String
     */
    public static String getMessage(String key) {
        String message = getMessages().getString(key);

        return ChatColor.translateAlternateColorCodes(
            '&',
            message == null ? key + " is missing." : message
        );
    }

    /**
     * Determine whether the message key exists.
     *
     * @param  key The message key.
     * @return boolean
     */
    public static boolean hasKey(String key) {
        String message = getMessages().getString(key);
        return message != null && !message.isEmpty();
    }

    /**
     * Retrieve the plural message.
     *
     * @param  questType The quest type.
     * @param  key The key.
     * @param  minecraftKeys The Minecraft keys.
     * @return String
     */
    public static String getPluralName(QuestType questType, String key, String... minecraftKeys) {
        String optionName;
        String questMessageKey = "quests." + questType.name().toLowerCase().replace("_", "-");
        String pluralKey = questMessageKey + ".item-plural." + key.toLowerCase();

        if (hasKey(pluralKey)) {
            return getMessage(pluralKey);
        }

        pluralKey = questMessageKey + ".item-plural.default";
        optionName = MinecraftLocaleConfig.getMinecraftName(key, minecraftKeys);

        if (hasKey(pluralKey)) {
            return MessageFormat.format(getMessage(pluralKey), optionName);
        }

        return optionName;
    }

    /**
     * Retrieve the messages.
     *
     * @return FileConfiguration
     */
    public static YamlDocument getMessages() {
        return messages;
    }

    /**
     * Reload the message configuration.
     */
    public static void reload() {
        try {
            messages.reload();
        } catch (Exception e) {
            //
        }
    }
}
