package de.stamme.basicquests.util;

import de.stamme.basicquests.config.Config;
import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.config.MinecraftLocaleConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.text.ChoiceFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StringFormatter {

    // Transforms Strings to a user-friendly format. E.g.: DIAMOND_PICKAXE -> Diamond Pickaxe
    public static String format(String string) {
        String[] words = string.split("_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String s = words[i];
            if (i != 0) {
                result.append(" ");
            }
            result.append(s.substring(0, 1).toUpperCase())
                .append(s.substring(1).toLowerCase());
        }
        return result.toString();
    }

    // returns a formatted & detailed description of an ItemStack
    public static String formatItemStack(ItemStack itemStack) {
        StringBuilder s = new StringBuilder();
        int amount = itemStack.getAmount();

        s.append(amount).append(" ");

        String itemName = MinecraftLocaleConfig.getMinecraftName(
            itemStack.getType().name(),
            "item.minecraft."
        );

        if (!itemStack.hasItemMeta()) {
            s.append(itemName);
            return s.toString();
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            s.append(itemName);
            return s.toString();
        }

        // Display name
        if (itemMeta.hasDisplayName()) {
            s.append(itemMeta.getDisplayName()).append(" ")
                .append(ChatColor.GRAY).append("(").append(itemName).append(")")
                .append(ChatColor.WHITE);
        } else {
            s.append(itemName);
        }

        // Enchanted book
        if (itemMeta instanceof EnchantmentStorageMeta meta) {
            appendEnchantments(
                s,
                meta.getStoredEnchants().entrySet(),
                meta.getStoredEnchants().size()
            );
            return s.toString();
        }

        // Potion (NEW API)
        if (itemMeta instanceof PotionMeta potionMeta) {
            s.append(": ").append(potionName(potionMeta.getBasePotionType()));
            return s.toString();
        }

        // Normal enchantments
        if (itemMeta.hasEnchants()) {
            appendEnchantments(
                s,
                itemMeta.getEnchants().entrySet(),
                itemMeta.getEnchants().size()
            );
        }

        return s.toString();
    }

    private static void appendEnchantments(
        StringBuilder s,
        Set<Map.Entry<Enchantment, Integer>> entries,
        int enchantments
    ) {
        s.append(": ");
        int i = 0;
        for (Map.Entry<Enchantment, Integer> entry : entries) {
            Enchantment enchantment = entry.getKey();
            s.append(enchantmentName(enchantment));

            String level = enchantmentLevel(entry.getValue(), enchantment);
            if (!level.isEmpty()) {
                s.append(" ").append(level);
            }

            if (++i < enchantments) {
                s.append(", ");
            }
        }
    }

    /**
     * NEW potion name resolver for 1.20+
     */
    public static String potionName(PotionType type) {
        if (type == null) {
            return "";
        }

        String key = type.name();

        boolean isLong = key.startsWith("LONG_");
        boolean isStrong = key.startsWith("STRONG_");

        if (isLong) {
            key = key.substring(5);
        } else if (isStrong) {
            key = key.substring(7);
        }

        String localized = MinecraftLocaleConfig.getMinecraftName(
            key,
            "effect.minecraft."
        );

        if (isStrong) {
            return localized + " II";
        }
        if (isLong) {
            return localized + " +";
        }

        return localized;
    }

    // returns the correct in game names for enchantments
    public static String enchantmentName(Enchantment e) {
        String name = e.getKey().getKey();
        return MinecraftLocaleConfig.getMinecraftName(name, "enchantment.minecraft.");
    }

    // returns the formatted level for enchantments
    public static String enchantmentLevel(int level, Enchantment enchantment) {
        if (MinecraftLocaleConfig.getMinecraftNames() == null) {
            return switch (level) {
                case 0 -> "";
                case 1 -> enchantment.getMaxLevel() == 1 ? "" : "I";
                case 2 -> "II";
                case 3 -> "III";
                case 4 -> "IV";
                case 5 -> "V";
                case 6 -> "VI";
                case 7 -> "VII";
                case 8 -> "VIII";
                case 9 -> "IX";
                case 10 -> "X";
                default -> String.valueOf(level);
            };
        }

        if (level == 1 && enchantment.getMaxLevel() == 1) {
            return "";
        }

        return level > 10
            ? String.valueOf(level)
            : MinecraftLocaleConfig.getMinecraftName(
            String.valueOf(level),
            "enchantment.level."
        );
    }

    public static String timeToMidnight() {
        LocalDateTime todayMidnight = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        LocalDateTime tomorrowMidnight = todayMidnight.plusDays(1);

        long diff = LocalDateTime.now().until(tomorrowMidnight, ChronoUnit.SECONDS);
        long minutes = (diff / 60) % 60;
        long hours = (diff / 3600) % 24;

        return String.format(
            Locale.ROOT,
            "%d:%02dh",
            hours,
            minutes
        );
    }

    public static String starString(int value, boolean shortForm) {
        String starChar = Config.getStarCharacter();
        if (shortForm) {
            return value + " " + starChar;
        }
        return starChar.repeat(Math.max(0, value));
    }

    public static String formatSkips(int count) {
        ChoiceFormat skipsFormat = new ChoiceFormat(
            new double[]{0, 1, 2},
            new String[]{
                MessagesConfig.getMessage("generic.skip.none"),
                MessagesConfig.getMessage("generic.skip.singular"),
                MessagesConfig.getMessage("generic.skip.plural"),
            }
        );
        return skipsFormat.format(count);
    }
}
