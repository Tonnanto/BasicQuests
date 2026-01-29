package de.stamme.basicquests.model.wrapper.material;

import org.bukkit.Material;

public abstract class QuestMaterialService {

    private static QuestMaterialService instance;

    public static QuestMaterialService getInstance() {
        if (instance == null) {
            // This version only support v1.21+
            instance = new QuestMaterialService_1_21();
        }
        return instance;
    }

    public abstract boolean isCorrectMaterialForQuest(Material questMaterial, Material blockMaterial);

    public abstract boolean isLogType(Material material);
}
