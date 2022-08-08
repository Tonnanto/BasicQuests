package de.stamme.basicquests.config;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.quests.QuestType;
import de.stamme.basicquests.util.StringFormatter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.MessageFormat;

public class MessagesConfig {
    private final BasicQuestsPlugin plugin;
    private static FileConfiguration messages;
    private File messagesFile;

    public MessagesConfig(BasicQuestsPlugin plugin, String locale) {
        this.plugin = plugin;
        this.register(locale);
    }

    /**
     * Register the messages configuration.
     */
    public void register(String locale) {
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
        return getMessages().getString(key);
    }

    /**
     * Determine whether the message key exists.
     *
     * @param  key The message key.
     * @return boolean
     */
    public static boolean keyExists(String key) {
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
        String questMessageKey = "quest." + StringFormatter.snakeToCamel(questType.name());
        String pluralKey = questMessageKey + "." + key.toLowerCase() + ".plural";

        if (keyExists(pluralKey)) {
            return getMessage(pluralKey);
        }

        pluralKey = questMessageKey + ".default.plural";
        optionName = MinecraftLocaleConfig.getMinecraftName(key, minecraftKeys);

        if (keyExists(pluralKey)) {
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

    /**
     * Retrieve the messages file.
     *
     * @return File
     */
    public File getMessagesFile() {
        return messagesFile;
    }
}