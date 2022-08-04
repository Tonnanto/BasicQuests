package de.stamme.basicquests.util;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.wrapper.material.QuestMaterialService;
import de.stamme.basicquests.model.wrapper.structure.QuestStructureType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

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
		
		if (itemStack.hasItemMeta()) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta == null) {
				s.append(localizedMaterial(itemStack.getType()));
				return s.toString();
			}

			// ItemStack has DisplayName ?
			if (itemMeta.hasDisplayName()) {
				s.append(itemMeta.getDisplayName()).append(" ");
				s.append(ChatColor.GRAY).append("(").append(localizedMaterial(itemStack.getType())).append(")").append(ChatColor.WHITE);
			} else if (!(itemMeta instanceof PotionMeta)) {
				s.append(localizedMaterial(itemStack.getType()));
			}


			if (itemMeta instanceof EnchantmentStorageMeta) {
				// Enchanted Book
				s.append(": ");
				int x = 0; // Purpose: Detect last Enchantment to leave out comma
				for (Map.Entry<Enchantment, Integer> entry : ((EnchantmentStorageMeta) itemMeta).getStoredEnchants().entrySet()) {
					s.append(localizedEnchantment(entry.getKey()));
					String enchantmentLevel = enchantmentLevel(entry.getKey(), entry.getValue());
					if (enchantmentLevel.length() > 0) { s.append(" ").append(enchantmentLevel); }
					x += 1;
					if (x < itemMeta.getEnchants().size()) { s.append(", "); }
				}

			} else if (itemMeta instanceof PotionMeta) {
				// Potion
//				s.append(": ");
				PotionData data = ((PotionMeta) itemMeta).getBasePotionData();

				s.append(localizedPotion(data));
				s.append(" ");
				if (data.isUpgraded())
					s.append("II ");
				if (data.isExtended())
					s.append("+");


			} else if (itemMeta.hasEnchants()) {
				// ItemStack has Enchantments ?

				s.append(": ");
				int x = 0; // Purpose: Detect last Enchantment to leave out comma
				for (Map.Entry<Enchantment, Integer> entry: itemMeta.getEnchants().entrySet()) {
					s.append(localizedEnchantment(entry.getKey()));
					String enchantmentLevel = enchantmentLevel(entry.getKey(), entry.getValue());
					if (enchantmentLevel.length() > 0) { s.append(" ").append(enchantmentLevel); }
					x += 1;
					if (x < itemMeta.getEnchants().size()) { s.append(", "); }
				}
			}
		} else {
			s.append(localizedMaterial(itemStack.getType()));
		}
		
		
		return s.toString();
	}

//	public static String potionName(PotionData data) {
//		String name;
//		if (data.getType() == PotionType.REGEN) { name = "REGENERATION"; }
//		else if (data.getType() == PotionType.JUMP) { name = "JUMP_BOOST"; }
//		else if (data.getType() == PotionType.INSTANT_HEAL) { name = "INSTANT_HEALTH"; }
//		else { name = data.getType().toString(); }
//		return format(name);
//	}
	
	// returns the formatted level for enchantments: 4 -> IV
	public static String enchantmentLevel(Enchantment enchantment, int lvl) {
		switch (lvl) {
		case 0: return "";
		case 1:
			if (enchantment.getMaxLevel() == 1) { return ""; }
			else return localizedMinecraftName("enchantment.level.1");
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
			return localizedMinecraftName("enchantment.level." + lvl);
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


	public static String localizedMaterial(Material material) {
		try {
			return localizedMinecraftName("block." + QuestMaterialService.getInstance().getTranslatableId(material));
		} catch (Exception ignored) {
			try {
				return localizedMinecraftName("item." + QuestMaterialService.getInstance().getTranslatableId(material));
			} catch (Exception alsoIgnored) {
				return StringFormatter.format(material.name());
			}
		}
	}

	public static String localizedEntity(EntityType entity) {
		try {
			return localizedMinecraftName("entity.minecraft." + entity.name().toLowerCase());
		} catch (Exception ignored) {
			return StringFormatter.format(entity.name());
		}
	}

	public static String localizedVillagerProfession(Villager.Profession profession) {
		try {
			if (profession == Villager.Profession.NONE)
				return localizedMinecraftName("entity.minecraft.villager");
			return localizedMinecraftName("entity.minecraft.villager." + profession.name().toLowerCase());
		} catch (Exception ignored) {
			return StringFormatter.format(profession.name());
		}
	}

	public static String localizedPotion(PotionData potionData) {
		String potionName = potionData.getType().name();
		try {
			return localizedMinecraftName("item.minecraft.potion.effect." + potionName.toLowerCase());
		} catch (Exception ignored) {
			return StringFormatter.format(potionName);
		}
	}

	public static String localizedStructure(QuestStructureType structureType) {
		try {
			return Main.l10n("structure." + structureType.name().toLowerCase());
		} catch (Exception ignored) {
			return StringFormatter.format(structureType.name());
		}
	}

	public static String localizedEnchantment(Enchantment enchantment) {
		String enchantmentName = enchantment.getKey().toString().split(":")[1];
		try {
			return localizedMinecraftName("enchantment.minecraft." + enchantmentName.toLowerCase());
		} catch (Exception ignored) {
			return StringFormatter.format(enchantmentName);
		}
	}

	public static String localizedMinecraftName(String id) {
		ResourceBundle bundle = ResourceBundle.getBundle("minecraft", Locale.getDefault());
		return bundle.getString(id);
	}
}
