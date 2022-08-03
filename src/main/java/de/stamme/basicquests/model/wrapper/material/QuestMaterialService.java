package de.stamme.basicquests.model.wrapper.material;

import de.stamme.basicquests.Main;
import org.bukkit.Material;

public abstract class QuestMaterialService {

    private static QuestMaterialService instance;

    public static QuestMaterialService getInstance() {
        if (instance == null) {
            switch (Main.getBukkitVersion()) {
                case v1_16:
                    instance = new QuestMaterialService_1_16();
                    break;
                case v1_17:
                case v1_18:
                    instance = new QuestMaterialService_1_17();
                    break;
                case v1_19:
                default:
                    instance = new QuestMaterialService_1_19();
                    break;
            }
        }
        return instance;
    }

    public abstract boolean isCorrectMaterialForQuest(Material questMaterial, Material blockMaterial);

    public abstract boolean isLogType(Material material);

    public String getTranslatableId(Material material) {
        switch (material) {
            case CARROT:
                return getTranslatableId(Material.CARROTS);
            case POTATO:
                return getTranslatableId(Material.POTATOES);
            case BEETROOT:
                return getTranslatableId(Material.BEETROOTS);
            case COCOA_BEANS:
                return getTranslatableId(Material.COCOA);
            default:
                return "minecraft." + material.name().toLowerCase();
        }
    }
}
