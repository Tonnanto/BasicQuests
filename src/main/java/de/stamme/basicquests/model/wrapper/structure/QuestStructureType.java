package de.stamme.basicquests.model.wrapper.structure;

import de.stamme.basicquests.config.MessagesConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public enum QuestStructureType {
    VILLAGE,
    MINESHAFT,
    FORTRESS,
    STRONGHOLD,
    JUNGLE_PYRAMID,
    OCEAN_RUIN,
    DESERT_PYRAMID,
    IGLOO,
    SWAMP_HUT,
    MONUMENT,
    END_CITY,
    MANSION,
    BURIED_TREASURE,
    SHIPWRECK,
    PILLAGER_OUTPOST,
    RUINED_PORTAL,
    BASTION_REMNANT,
    ANCIENT_CITY,
    TRAIL_RUINS,
    TRAIL_CHAMBERS;

    private final String localizedName;

    QuestStructureType() {
        this.localizedName = MessagesConfig.getMessage("quests.find-structure.structure." + this.name().toLowerCase());
    }

    /**
     * @param structureName the name of the structure
     * @return the spigot 1.20 equivalent of the Structure
     */
    @Nullable
    public static QuestStructureType fromString(String structureName) {
        return QuestStructureService.getInstance().fromString(structureName);
    }

    @Nullable
    public Location findNearLocation(Location nearLocation, World world) {
        return QuestStructureService.getInstance().findStructureNearLocation(this, nearLocation, world);
    }

    public String getLocalizedName() {
        return this.localizedName;
    }
}
