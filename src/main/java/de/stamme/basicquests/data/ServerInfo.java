package de.stamme.basicquests.data;

import de.stamme.basicquests.main.Main;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ServerInfo {

    private static final String path = Main.plugin.getDataFolder() +  "/server_info.data";
    private static HashMap<String, Object> map;

    public static void save() {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(path)));
            out.writeObject(map);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        Object obj = null;

        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(path)));
            obj = in.readObject();

        } catch (Exception ignored) {}

        if (obj == null) {
            map = new HashMap<>();
            return;
        }

        if (obj instanceof HashMap<?, ?>) {
            map = (HashMap<String, Object>) obj;
        }
    }

    public static Object get(String key) {
        if (map == null) return null;
        return map.get(key);
    }

    public static void put(String key, Object value) {
        map.put(key, value);
    }

    // Specific getter
    public static LocalDateTime getLastSkipReset() {
        Object obj = get("lastSkipReset");
        if (obj instanceof LocalDateTime)
            return (LocalDateTime) obj;
        return null;
    }
}
