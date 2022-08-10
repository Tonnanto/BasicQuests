package de.stamme.basicquests.config;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.quests.QuestType;
import de.stamme.basicquests.util.StringFormatter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.MessageFormat;

public class MessagesConfig {
    private static FileConfiguration messages;
    private static File messagesFile;

    /**
     * Register the messages configuration.
     */
    public static void register(String locale) {
        BasicQuestsPlugin plugin = BasicQuestsPlugin.getPlugin();
        String filePath = "lang/messages_" + locale + ".yml";

        messagesFile = new File(
            plugin.getDataFolder(),
            filePath
        );

        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            plugin.saveResource(filePath, false);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);
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
     * Retrieve the messages configuration.
     *
     * @return FileConfiguration
     */
    public static FileConfiguration getMessages() {
        return messages;
    }
}
