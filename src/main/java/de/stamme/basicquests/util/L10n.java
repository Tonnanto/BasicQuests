package de.stamme.basicquests.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.Config;
import de.stamme.basicquests.model.quests.QuestType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class L10n {

    // Content of a minecraft localization file with the configured locale
    private static Map<String, String> minecraftNames;

    public static Map<String, String> getMinecraftNames() {
        return minecraftNames;
    }

    /**
     * Get localized value of a minecraft name
     * @param name the name of the translatable id
     * @param keys the keys to look in for the name (appended "." expected!)
     * @return a localized string
     */
    public static String getMinecraftName(String name, String... keys) {
        if (minecraftNames != null) {
            for (String minecraftKey : keys) {
                String minecraftName = minecraftNames.get(minecraftKey + name.toLowerCase());
                if (minecraftName != null) return minecraftName;
            }
        }
        return StringFormatter.format(name);
    }

    /**
     * Get localized value from the 'messages' Resource Bundle
     * @param key key of message
     * @return localized string
     */
    public static String getMessage(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        return bundle.getString(key);
    }

    /**
     * @param key key of message
     * @return whether the key exists within the 'messages' Resource Bundle
     */
    public static boolean keyExists(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        return bundle.containsKey(key);
    }

    public static String getLocalizedPluralName(QuestType questType, String key, String... minecraftKeys) {
        String optionName;
        String questMessageKey = "quest." + StringFormatter.snakeToCamel(questType.name());
        String pluralKey = questMessageKey + "." + key.toLowerCase() + ".plural";
        if (keyExists(pluralKey)) {
            optionName = getMessage(pluralKey);
        } else {
            pluralKey = questMessageKey + ".default.plural";
            optionName = getMinecraftName(key, minecraftKeys);
            if (keyExists(pluralKey)) {
                optionName = MessageFormat.format(getMessage(pluralKey), optionName);
            }
        }
        return optionName;
    }

    // TODO: To get en_us you need to download the full .jar file of the server and get it from there.
    public static void init() {
        try {
            String locale = Config.getMinecraftItemsLocale();
            if (locale == null) {
                minecraftNames = null;
            } else {
                minecraftNames = new HashMap<>();
                BasicQuestsPlugin plugin = BasicQuestsPlugin.getPlugin();
                File localesFolder = new File(plugin.getDataFolder(), "locales");
                File localeFile = new File(localesFolder, locale + ".json");
                Path localePath = localeFile.toPath();
                if (localeFile.exists() && checkLocaleFile(localePath)) {
                    loadLocale(localePath);
                } else {
                    Path localesPath = localesFolder.toPath();
                    plugin.getLogger().info("Downloading minecraft locale \"" + locale + "\" ...");
                    if (!localesFolder.exists()) {
                        Files.createDirectories(localesPath);
                    }

                    JsonObject versionManifest = getElement("https://launchermeta.mojang.com/mc/game/version_manifest.json").getAsJsonObject();
                    String latestVersion = versionManifest.getAsJsonObject("latest").get("release").getAsString();
                    JsonObject assetsObjects = null;
                    for (JsonElement versions : versionManifest.getAsJsonArray("versions")) {
                        JsonObject version = versions.getAsJsonObject();
                        String versionID = version.get("id").getAsString();
                        if (versionID.equals(latestVersion)) {
                            JsonObject manifest = getElement(version.get("url").getAsString()).getAsJsonObject();
                            JsonObject assets = getElement(manifest.getAsJsonObject("assetIndex").get("url").getAsString()).getAsJsonObject();
                            assetsObjects = assets.getAsJsonObject("objects");
                            break;
                        }
                    }

                    if (assetsObjects == null) {
                        throw new RuntimeException("HOLY SHIT");
                    }

                    String needed = "minecraft/lang/" + locale + ".json";
                    for (Map.Entry<String, JsonElement> asset : assetsObjects.entrySet()) {
                        if (asset.getKey().equalsIgnoreCase(needed)) {
                            String hash = asset.getValue().getAsJsonObject().get("hash").getAsString();
                            HttpURLConnection connection = (HttpURLConnection) new URL(
                                    "https://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash
                            ).openConnection();
                            InputStream inputStream = connection.getInputStream();
                            Files.copy(inputStream, localePath, StandardCopyOption.REPLACE_EXISTING);
                            loadLocale(localePath);
                            connection.disconnect();
                            return;
                        }
                    }

                    throw new RuntimeException("THERE IS NO LOCALE NAMED " + locale);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean checkLocaleFile(Path path) {
        try {
            int updatePeriod = Config.getMinecraftItemsLocaleUpdatePeriod();
            if (updatePeriod <= 0) {
                return true;
            } else {
                BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - attributes.creationTime().toMillis()) < updatePeriod;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadLocale(Path path) {
        try {
            for (Map.Entry<String, JsonElement> locale : JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject().entrySet()) {
                minecraftNames.put(locale.getKey(), locale.getValue().getAsString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonElement getElement(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.getResponseCode();
            InputStream errorStream = connection.getErrorStream();
            if (errorStream == null) {
                JsonElement element = JsonParser.parseReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                connection.disconnect();
                return element;
            } else {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int length;
                while ((length = errorStream.read(buf)) != -1) {
                    result.write(buf, 0, length);
                }

                connection.disconnect();
                throw new RuntimeException("\n\n" + result.toString(StandardCharsets.UTF_8.name()) + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
