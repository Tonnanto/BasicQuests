package de.stamme.basicquests.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.stamme.basicquests.Config;
import de.stamme.basicquests.Main;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class StringFormatter {

	private static Map<String, String> LOCALE;

	public static String getLocalizedName(String name, String key) {
		if (LOCALE == null) {
			return format(name);
		} else {
			return LOCALE.get(key + name.toLowerCase());
		}
	}

	// Transforms Strings to a user friendly format. p.e: DIAMOND_PICKAXE -> Diamond Pickaxe
	public static String format(String string) {
		String[] words = string.split("_");
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < words.length; i++) {
			String s = words[i];
			if(i != 0) { result.append(" "); }
			result.append(s.substring(0, 1).toUpperCase()).append(s.substring(1).toLowerCase());
		}
		return result.toString();
	}
	
	// returns a formatted & detailed description of an ItemStack
	// format: 	<amount> <DisplayName> (<MaterialName>): <Enchantment1>, <Enchantment2>
	// e.g: 	Destroyer (Diamond Sword): Sharpness V, Fire Aspect II
	// e.g:		32 Books
	// e.g:		1 Enchanted Book: Power V
	public static String formatItemStack(ItemStack itemStack) {
		StringBuilder s = new StringBuilder();
		int amount = itemStack.getAmount();

		s.append(amount).append(" ");
		
		String itemName = getLocalizedName(itemStack.getType().name(), "item.minecraft.");
		if (itemStack.hasItemMeta()) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta == null) {
				s.append(format(itemStack.getType().toString()));
				return s.toString();
			}

			// ItemStack has DisplayName ?
			if (itemMeta.hasDisplayName()) {
				s.append(itemMeta.getDisplayName()).append(" ");
				s.append(ChatColor.GRAY).append("(").append(itemName).append(")").append(ChatColor.WHITE);
			} else {
				s.append(itemName);
			}


			if (itemMeta instanceof EnchantmentStorageMeta) {
				// Enchanted Book
				appendEnchantments(s, ((EnchantmentStorageMeta) itemMeta).getStoredEnchants().entrySet(), itemMeta.getEnchants().size());
			} else if (itemMeta instanceof PotionMeta) {
				// Potion
				s.append(": ");
				PotionData data = ((PotionMeta) itemMeta).getBasePotionData();

				s.append(potionName(data));
				s.append(" ");
				if (data.isUpgraded())
					s.append("II ");
				if (data.isExtended())
					s.append("+");


			} else if (itemMeta.hasEnchants()) {
				// ItemStack has Enchantments ?
				appendEnchantments(s, itemMeta.getEnchants().entrySet(), itemMeta.getEnchants().size());
			}
		} else {
			s.append(itemName);
		}
		
		
		return s.toString();
	}

	private static void appendEnchantments(StringBuilder s, Set<Map.Entry<Enchantment, Integer>> entries, int enchantments) {
		s.append(": ");
		int i = 0; // Purpose: Detect last Enchantment to leave out comma
		for (Map.Entry<Enchantment, Integer> entry : entries) {
			Enchantment enchantment = entry.getKey();
			s.append(enchantmentName(enchantment));
			String enchantmentLevel = enchantmentLevel(entry.getValue(), enchantment);
			if (enchantmentLevel.length() > 0) { s.append(" ").append(enchantmentLevel); }
			++i;
			if (i < enchantments) { s.append(", "); }
		}
	}

	public static String potionName(PotionData data) {
		String name;
		if (data.getType() == PotionType.REGEN) { name = "REGENERATION"; }
		else if (data.getType() == PotionType.JUMP) { name = "JUMP_BOOST"; }
		else if (data.getType() == PotionType.INSTANT_HEAL) { name = "INSTANT_HEALTH"; }
		else { name = data.getType().name(); }
		return getLocalizedName(name, "effect.minecraft.");
	}
	
	// returns the correct in game names for enchantments
	public static String enchantmentName(Enchantment e) {
		
		String name = e.getKey().toString().split(":")[1];
		
		if (name.equalsIgnoreCase("SWEEPING")) { name = "SWEEPING_EDGE"; }
		
		return getLocalizedName(name, "enchantment.minecraft.");
	}
	
	// returns the formatted level for enchantments: 4 -> IV
	public static String enchantmentLevel(int level, Enchantment enchantment) {
		if (LOCALE == null) {
			switch (level) {
				case 0: return "";
				case 1: if (enchantment.getMaxLevel() == 1) { return ""; } else return "I";
				case 2: return "II";
				case 3: return "III";
				case 4: return "IV";
				case 5: return "V";
				case 6: return "VI";
				case 7: return "VII";
				case 8: return "VIII";
				case 9: return "IX";
				case 10: return "X";
				default: return String.valueOf(level);
			}
		} else {
			return level > 10 ? String.valueOf(level) : level == 1 && enchantment.getMaxLevel() == 1 ? "" : LOCALE.get("enchantment.level." + level);
		}
	}
	
	public static String timeToMidnight() {
		LocalDateTime todayMidnight = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
		LocalDateTime tomorrowMidnight = todayMidnight.plusDays(1);
		
		long diff_in_sec = LocalDateTime.now().until(tomorrowMidnight, ChronoUnit.SECONDS);
		long m = (diff_in_sec / (60)) % 60; 
		long h = (diff_in_sec / (60 * 60)) % 24; 
		
		return String.format("%s:%s%sh", h, (m > 9) ? "" : "0", m);
	}

	// TODO: To get en_us you need to download the full .jar file of the server and get it from there.
	public static void init() {
		try {
			String locale = Config.getMojangItemsLocale();
			if (locale == null) {
				LOCALE = null;
			} else {
				LOCALE = new HashMap<>();
				Main plugin = Main.getPlugin();
				File localesFolder = new File(plugin.getDataFolder(), "locales");
				File localeFile = new File(localesFolder, locale + ".json");
				Path localePath = localeFile.toPath();
				if (localeFile.exists() && checkLocaleFile(localePath)) {
					loadLocale(localePath);
				} else {
					Path localesPath = localesFolder.toPath();
					plugin.getLogger().info("Downloading mojang locale...");
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
			int updatePeriod = Config.getMojangItemsLocaleUpdatePeriod();
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
				LOCALE.put(locale.getKey(), locale.getValue().getAsString());
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
