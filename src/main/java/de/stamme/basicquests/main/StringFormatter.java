package de.stamme.basicquests.main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

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
	// p.e: 	Zerstörer (Diamond Sword): Sharpness V, Fire Aspect II
	// p.e:		32 Books
	public static String formatItemStack(ItemStack itemStack) {
		StringBuilder s = new StringBuilder();
		int amount = itemStack.getAmount();
		
		s.append(amount).append(" ");
		
		if (itemStack.hasItemMeta()) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta == null) {
				s.append(format(itemStack.getType().toString()));
				return s.toString();
			}

			// ItemStack has DisplayName ?
			if (itemMeta.hasDisplayName()) {
				s.append(itemMeta.getDisplayName()).append(" ");
				s.append(ChatColor.GRAY).append("(").append(format(itemStack.getType().toString())).append(")").append(ChatColor.WHITE);
			} else {
				s.append(format(itemStack.getType().toString()));
			}


			if (itemMeta instanceof EnchantmentStorageMeta) {
//				Enchanted Book
				s.append(": ");
				int x = 0; // Purpose: Detect last Enchantment to leave out comma
				for (Map.Entry<Enchantment, Integer> entry : ((EnchantmentStorageMeta) itemMeta).getStoredEnchants().entrySet()) {
					s.append(enchantmentName(entry.getKey()));
					String enchantmentLevel = enchantmentLevel(entry.getKey(), entry.getValue());
					if (enchantmentLevel.length() > 0) { s.append(" ").append(enchantmentLevel); }
					x += 1;
					if (x < itemMeta.getEnchants().size()) { s.append(", "); }
				}

			} else if (itemMeta.hasEnchants()) {
				// ItemStack has Enchantments ?

				s.append(": ");
				int x = 0; // Purpose: Detect last Enchantment to leave out comma
				for (Map.Entry<Enchantment, Integer> entry: itemMeta.getEnchants().entrySet()) {
					s.append(enchantmentName(entry.getKey()));
					String enchantmentLevel = enchantmentLevel(entry.getKey(), entry.getValue());
					if (enchantmentLevel.length() > 0) { s.append(" ").append(enchantmentLevel); }
					x += 1;
					if (x < itemMeta.getEnchants().size()) { s.append(", "); }
				}
			}
		} else {
			s.append(format(itemStack.getType().toString()));
		}
		
		
		return s.toString();
	}
	
	// returns the correct ingame names for enchantments
	public static String enchantmentName(Enchantment e) {
		
		String name = e.getKey().toString().split(":")[1];
		
		if (name.equalsIgnoreCase("SWEEPING")) { name = "SWEEPING_EDGE"; }
		
		return format(name);
	}
	
	// returns the formatted level for enchantments: 4 -> IV
	public static String enchantmentLevel(Enchantment enchantment, int lvl) {
		switch (lvl) {
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
		default: return "" + lvl;
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
}
