package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.config.MinecraftLocaleConfig;
import de.stamme.basicquests.model.rewards.Reward;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class FishItemQuest extends Quest {

    public enum Option {
        MATERIAL,
        ANY_FISH,
        ANY_TREASURE,
        ANY_ITEM
    }


    // ---------------------------------------------------------------------------------------
    // Quest State
    // ---------------------------------------------------------------------------------------

    @Nullable
    private final Material material;
    private final Option option;


    // ---------------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------------

    public FishItemQuest(@NotNull Material material, int goal, Reward reward) {
        super(goal, reward);
        this.material = material;
        this.option = Option.MATERIAL;
    }

    public FishItemQuest(Option option, int goal, Reward reward) {
        super(goal, reward);
        this.material = null;
        this.option = option;
    }


    // ---------------------------------------------------------------------------------------
    // Functionality
    // ---------------------------------------------------------------------------------------

    @Override
    public QuestData toData() {
        QuestData data = super.toData();
        data.setQuestType(QuestType.FISH_ITEM.name());
        if (material != null) {
            data.setMaterial(material.name());
        }
        data.setOption(option.name());
        return data;
    }

    public boolean itemMatches(Item item) {
        Material caughtMaterial = item.getItemStack().getType();
        if (material != null) {
            return item.getItemStack().getType() == material;
        }

        List<String> fish = Arrays.asList("COD", "SALMON", "PUFFERFISH", "TROPICAL_FISH");
        if (option == Option.ANY_FISH) {
            return fish.contains(caughtMaterial.name());
        }

        List<String> treasures = Arrays.asList("BOW", "ENCHANTED_BOOK", "NAME_TAG", "NAUTILUS_SHELL", "SADDLE");
        if (option == Option.ANY_TREASURE) {
            // Enchanted fishing rods count towards treasure while others do not.
            boolean isEnchantedFishingRod = false;
            if (caughtMaterial == Material.FISHING_ROD) {
                ItemMeta fishingRodMeta = item.getItemStack().getItemMeta();
                isEnchantedFishingRod = fishingRodMeta != null && fishingRodMeta.hasEnchants();
            }
            return treasures.contains(caughtMaterial.name()) || isEnchantedFishingRod;
        }

        if (option == Option.ANY_ITEM) {
            // Everything that is not a fish is a valid item
            return !fish.contains(caughtMaterial.name());
        }
        return false;
    }


    // ---------------------------------------------------------------------------------------
    // Getter & Setter
    // ---------------------------------------------------------------------------------------

    /**
     * @return String in the format: "Kill <amount> <entity>"
     */
    @Override
    public String getName() {
        int goal = this.getGoal();

        if (option == Option.ANY_FISH) {
            if (goal <= 1) {
                return MessagesConfig.getMessage("quests.fish-item.any-fish.singular");
            } else {
                return MessageFormat.format(MessagesConfig.getMessage("quests.fish-item.any-fish.plural"), goal);
            }
        }

        if (option == Option.ANY_TREASURE) {
            if (goal <= 1) {
                return MessagesConfig.getMessage("quests.fish-item.any-treasure.singular");
            } else {
                return MessageFormat.format(MessagesConfig.getMessage("quests.fish-item.any-treasure.plural"), goal);
            }
        }

        if (option == Option.ANY_ITEM) {
            if (goal <= 1) {
                return MessagesConfig.getMessage("quests.fish-item.any-item.singular");
            } else {
                return MessageFormat.format(MessagesConfig.getMessage("quests.fish-item.any-item.plural"), goal);
            }
        }


        if (goal <= 1) {
            String singularName = MinecraftLocaleConfig.getMinecraftName(getOptionKey(), "entity.minecraft.", "item.minecraft.");
            return MessageFormat.format(MessagesConfig.getMessage("quests.fish-item.singular"), singularName);
        } else {
            String pluralName = MessagesConfig.getPluralName(getQuestType(), getOptionKey(), "entity.minecraft.", "item.minecraft.");
            return MessageFormat.format(MessagesConfig.getMessage("quests.fish-item.plural"), goal, pluralName);
        }
    }

    public String[] getOptionNames() {
        return new String[]{QuestType.FISH_ITEM.name(), getOptionKey()};
    }

    public @Nullable Material getMaterial() {
        return material;
    }

    @Override
    public final QuestType getQuestType() {
        return QuestType.FISH_ITEM;
    }

    @Override
    public String getOptionKey() {
        if (material != null) {
            return material.name();
        }
        return option.name();
    }
}
