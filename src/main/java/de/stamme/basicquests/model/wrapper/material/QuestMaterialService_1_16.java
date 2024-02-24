package de.stamme.basicquests.model.wrapper.material;

import org.bukkit.Material;

public class QuestMaterialService_1_16 extends QuestMaterialService {
    @Override
    public boolean isCorrectMaterialForQuest(Material questMaterial, Material blockMaterial) {
        return questMaterial == blockMaterial;
    }

    @Override
    public boolean isLogType(Material material) {
        return (material == Material.ACACIA_LOG ||
            material == Material.BIRCH_LOG ||
            material == Material.DARK_OAK_LOG ||
            material == Material.JUNGLE_LOG ||
            material == Material.OAK_LOG ||
            material == Material.SPRUCE_LOG);
    }
}
