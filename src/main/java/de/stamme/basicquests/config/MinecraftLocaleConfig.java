package de.stamme.basicquests.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.wrapper.BukkitVersion;
import de.stamme.basicquests.util.StringFormatter;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MinecraftLocaleConfig {
    public static Map<String, String> minecraftNames;

    /**
     * Initialize the Minecraft items locale.
     */
    public static void register() {
        try {
            String locale = Config.getMinecraftItemsLocale();

            if (locale == null) {
                locale = getMinecraftItemsLocaleForPluginLocale(Config.getLocale());
            }

            if (locale == null) {
                minecraftNames = null;
                return;
            }

            minecraftNames = new HashMap<>();
            BasicQuestsPlugin plugin = BasicQuestsPlugin.getPlugin();
            File localesFolder = new File(plugin.getDataFolder(), "locales");
            File localeFile = new File(localesFolder, locale + ".json");
            Path localePath = localeFile.toPath();

            if (localeFile.exists() && checkLocaleFile(localePath)) {
                loadLocale(localePath);
                return;
            }

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

            throw new RuntimeException("There is no locale named " + locale);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Uses the locale config option to determine the correct locale for minecraft item names
     *
     * @param pluginLocale locale string as found in the config file
     * @return minecraft item locale to use
     */
    @Nullable
    private static String getMinecraftItemsLocaleForPluginLocale(String pluginLocale) {
        if (pluginLocale.contains("_") && !pluginLocale.equals("en_us")) {
            return pluginLocale;
        }

        switch (pluginLocale) {
            case "de":
                return "de_de";
            case "es":
                return "es_es";
            case "fr":
                return "fr_fr";
            case "ru":
                return "ru_ru";
            default:
                return null; // Defaults to null and uses english item names
        }
    }

    /**
     * Check the Minecraft locale file.
     *
     * @param path The path.
     * @return boolean
     */
    public static boolean checkLocaleFile(Path path) {
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

    /**
     * Load the Minecraft locale.
     *
     * @param path The path.
     */
    public static void loadLocale(Path path) {
        try {
            for (Map.Entry<String, JsonElement> locale : getJsonElementFromReader(Files.newBufferedReader(path)).getAsJsonObject().entrySet()) {
                minecraftNames.put(locale.getKey(), locale.getValue().getAsString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the Json element.
     *
     * @param url The URL.
     * @return JsonElement
     */
    public static JsonElement getElement(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.getResponseCode();

            InputStream errorStream = connection.getErrorStream();

            if (errorStream == null) {
                Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                JsonElement element = getJsonElementFromReader(reader);

                connection.disconnect();

                return element;
            }

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int length;

            while ((length = errorStream.read(buf)) != -1) {
                result.write(buf, 0, length);
            }

            connection.disconnect();
            throw new RuntimeException("\n\n" + result.toString(StandardCharsets.UTF_8.name()) + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Uses the compatible gson method to parse a reader to a JsonElement
     *
     * @param reader the reader to parse
     * @return the resulting JsonElement
     */
    private static JsonElement getJsonElementFromReader(Reader reader) {
        JsonElement element;

        // spigot v1.16 - v1.17 comes with an older gson version that does not support JsonParser.parseReader
        if (BasicQuestsPlugin.getBukkitVersion() == BukkitVersion.v1_16 ||
            BasicQuestsPlugin.getBukkitVersion() == BukkitVersion.v1_17) {
            element = new JsonParser().parse(reader);
        } else {
            element = JsonParser.parseReader(reader);
        }

        return element;
    }

    /**
     * Get localized value of a minecraft name
     *
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
     * Retrieve the Minecraft localized names.
     *
     * @return Map
     */
    public static Map<String, String> getMinecraftNames() {
        return minecraftNames;
    }
}
