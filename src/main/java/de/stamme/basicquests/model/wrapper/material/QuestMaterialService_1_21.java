package de.stamme.basicquests.model.wrapper.material;

import java.util.EnumMap;
import java.util.EnumSet;

import org.bukkit.Material;

public class QuestMaterialService_1_21 extends QuestMaterialService {

    private static final EnumMap<Material, EnumSet<Material>> ORE_VARIANTS = new EnumMap<>(Material.class);
    private static final EnumSet<Material> LOG_MATERIALS = EnumSet.of(Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG,
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.MANGROVE_LOG, Material.CHERRY_LOG, Material.PALE_OAK_LOG);

    static {
        ORE_VARIANTS.put(Material.LAPIS_ORE, EnumSet.of(Material.DEEPSLATE_LAPIS_ORE));
        ORE_VARIANTS.put(Material.IRON_ORE, EnumSet.of(Material.DEEPSLATE_IRON_ORE));
        ORE_VARIANTS.put(Material.COAL_ORE, EnumSet.of(Material.DEEPSLATE_COAL_ORE));
        ORE_VARIANTS.put(Material.COPPER_ORE, EnumSet.of(Material.DEEPSLATE_COPPER_ORE));
        ORE_VARIANTS.put(Material.DIAMOND_ORE, EnumSet.of(Material.DEEPSLATE_DIAMOND_ORE));
        ORE_VARIANTS.put(Material.EMERALD_ORE, EnumSet.of(Material.DEEPSLATE_EMERALD_ORE));
        ORE_VARIANTS.put(Material.GOLD_ORE, EnumSet.of(Material.DEEPSLATE_GOLD_ORE));
        ORE_VARIANTS.put(Material.REDSTONE_ORE, EnumSet.of(Material.DEEPSLATE_REDSTONE_ORE));
    }

    @Override
    public boolean isCorrectMaterialForQuest(Material questMaterial, Material blockMaterial) {
        if (questMaterial == blockMaterial) {
            return true;
        }
        return ORE_VARIANTS.getOrDefault(questMaterial, EnumSet.noneOf(Material.class)).contains(blockMaterial);
    }

    @Override
    public boolean isLogType(Material material) {
        return LOG_MATERIALS.contains(material);
    }
}
