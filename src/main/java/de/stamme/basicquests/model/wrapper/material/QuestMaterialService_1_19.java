package de.stamme.basicquests.model.wrapper.material;

import org.bukkit.Material;

// 1.19 introduces Mangrove Logs
public class QuestMaterialService_1_19 extends QuestMaterialService {
    @Override
    public boolean isCorrectMaterialForQuest(Material questMaterial, Material blockMaterial) {
        return (questMaterial == blockMaterial ||
            (questMaterial == Material.LAPIS_ORE && blockMaterial == Material.DEEPSLATE_LAPIS_ORE) ||
            (questMaterial == Material.IRON_ORE && blockMaterial == Material.DEEPSLATE_IRON_ORE) ||
            (questMaterial == Material.COAL_ORE && blockMaterial == Material.DEEPSLATE_COAL_ORE) ||
            (questMaterial == Material.COPPER_ORE && blockMaterial == Material.DEEPSLATE_COPPER_ORE) ||
            (questMaterial == Material.DIAMOND_ORE && blockMaterial == Material.DEEPSLATE_DIAMOND_ORE) ||
            (questMaterial == Material.EMERALD_ORE && blockMaterial == Material.DEEPSLATE_EMERALD_ORE) ||
            (questMaterial == Material.GOLD_ORE && blockMaterial == Material.DEEPSLATE_GOLD_ORE) ||
            (questMaterial == Material.REDSTONE_ORE && blockMaterial == Material.DEEPSLATE_REDSTONE_ORE));
    }

    @Override
    public boolean isLogType(Material material) {
        return (material == Material.ACACIA_LOG ||
            material == Material.BIRCH_LOG ||
            material == Material.DARK_OAK_LOG ||
            material == Material.JUNGLE_LOG ||
            material == Material.OAK_LOG ||
            material == Material.SPRUCE_LOG ||
            material == Material.MANGROVE_LOG);
    }
}
