package de.stamme.basicquests.main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class StringFormatter {

	// Transforms Strings to a user friendly format. p.e: DIAMOND_PICKAXE -> Diamond Pickaxe
	public static String format(String string) {
		String[] words = string.split("_");
		String result = "";
		for(int i = 0; i < words.length; i++) {
			String s = words[i];
			if(i != 0) { result += " "; }
			result += s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
		}
		return result;
	}
	
	// returns a formatted & detailed description of an ItemStack
	// format: 	<amount> <DisplayName> (<MaterialName>): <Enchantment1>, <Enchantment2>
	// p.e: 	Zerstörer (Diamond Sword): Sharpness V, Fire Aspect II
	// p.e:		32 Books
	public static String formatItemStack(ItemStack itemStack) {
		String s = "";
		int amount = itemStack.getAmount();
		
		s += amount + " ";
		
		if (itemStack.hasItemMeta()) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			
			// ItemStack has DisplayName ?
			if (itemMeta.hasDisplayName()) {
				s += itemMeta.getDisplayName() + " ";
				s += ChatColor.GRAY + "(" + format(itemStack.getType().toString()) + ")" + ChatColor.WHITE;
			} else {
				s += format(itemStack.getType().toString());
			}
			
			// ItemStack has Enchantments ?
			if (itemMeta.hasEnchants()) {
				s += ": ";
				int x = 0; // Purpose: Detect last Enchantment to leave out comma
				for (Map.Entry<Enchantment, Integer> entry: itemMeta.getEnchants().entrySet()) {
					s += enchantmentName(entry.getKey());
					String enchantmentLevel = enchantmentLevel(entry.getKey(), entry.getValue());
					if (enchantmentLevel.length() > 0) { s += " " + enchantmentLevel; }
					x += 1;
					if (x < itemMeta.getEnchants().size()) { s += ", "; }
				}
			}
		} else {
			s += format(itemStack.getType().toString());
		}
		
		
		return s;
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
