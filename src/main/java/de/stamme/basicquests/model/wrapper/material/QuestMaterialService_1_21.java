package de.stamme.basicquests.model.wrapper.material;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;

public class QuestMaterialService_1_21 extends QuestMaterialService {

    private static final Set<Material> ORES = EnumSet.of(
        Material.COAL_ORE,
        Material.DEEPSLATE_COAL_ORE,
        Material.IRON_ORE,
        Material.DEEPSLATE_IRON_ORE,
        Material.COPPER_ORE,
        Material.DEEPSLATE_COPPER_ORE,
        Material.GOLD_ORE,
        Material.DEEPSLATE_GOLD_ORE,
        Material.REDSTONE_ORE,
        Material.DEEPSLATE_REDSTONE_ORE,
        Material.LAPIS_ORE,
        Material.DEEPSLATE_LAPIS_ORE,
        Material.DIAMOND_ORE,
        Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE,
        Material.DEEPSLATE_EMERALD_ORE
    );

    private static final Set<Material> LOGS = EnumSet.of(
        Material.ACACIA_LOG,
        Material.BIRCH_LOG,
        Material.CHERRY_LOG,
        Material.DARK_OAK_LOG,
        Material.JUNGLE_LOG,
        Material.MANGROVE_LOG,
        Material.OAK_LOG,
        Material.SPRUCE_LOG
    );

    @Override
    public boolean isCorrectMaterialForQuest(Material questMaterial, Material blockMaterial) {
        if (questMaterial == blockMaterial) {
            return true;
        }

        // obsługa ore ↔ deepslate_ore
        return ORES.contains(questMaterial) && ORES.contains(blockMaterial)
            && questMaterial.name().endsWith("_ORE")
            && blockMaterial.name().endsWith("_ORE");
    }

    @Override
    public boolean isLogType(Material material) {
        return LOGS.contains(material);
    }
}
