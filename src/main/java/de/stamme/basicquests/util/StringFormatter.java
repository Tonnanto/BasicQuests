package de.stamme.basicquests.util;

import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.config.MinecraftLocaleConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

public class StringFormatter {

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

		String itemName = MinecraftLocaleConfig.getMinecraftName(itemStack.getType().name(), "item.minecraft.");
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
		return MinecraftLocaleConfig.getMinecraftName(name, "effect.minecraft.");
	}

	// returns the correct in game names for enchantments
	public static String enchantmentName(Enchantment e) {
		String name = e.getKey().toString().split(":")[1];
		return MinecraftLocaleConfig.getMinecraftName(name, "enchantment.minecraft.");
	}

	// returns the formatted level for enchantments: 4 -> IV
	public static String enchantmentLevel(int level, Enchantment enchantment) {
		if (MinecraftLocaleConfig.getMinecraftNames() == null) {
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
			return level > 10 ? String.valueOf(level) : level == 1 && enchantment.getMaxLevel() == 1 ? "" : MinecraftLocaleConfig.getMinecraftName(level + "", "enchantment.level.");
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

	public static String starString(int value, boolean shortForm) {
	    String starChar = Config.getStarCharacter();
        if (shortForm) {
	        // 5 *
	        return value + " " + starChar;
        }

	    // *****
        return new String(new char[value]).replace("\0", starChar);
    }
}
