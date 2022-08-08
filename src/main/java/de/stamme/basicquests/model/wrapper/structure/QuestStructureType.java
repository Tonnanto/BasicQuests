package de.stamme.basicquests.model.wrapper.structure;


import de.stamme.basicquests.BasicQuestsPlugin;
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
    ANCIENT_CITY;

    private final String localizedName;

    QuestStructureType() {
        this.localizedName = MessagesConfig.getMessage("quest.findStructure.structure." + this.name().toLowerCase());
    }

    /**
     * @param structureName the name of the structure
     * @return the spigot 1.19 equivalent of the Structure
     */
    @Nullable
    public static QuestStructureType fromString(String structureName) {
        return getQuestStructureService().fromString(structureName);
    }

    @Nullable
    public Location findNearLocation(Location nearLocation, World world) {
        return getQuestStructureService().findStructureNearLocation(this, nearLocation, world);
    }

    /**
     * @return the QuestStructureService that handles Structures correctly for the current spigot version of the server.
     */
    private static QuestStructureService getQuestStructureService() {
        switch (BasicQuestsPlugin.getBukkitVersion()) {
            case v1_16:
            case v1_17:
                return new QuestStructureService_1_16();
            case v1_18:
                return new QuestStructureService_1_18();
            case v1_19:
            default:
                return new QuestStructureService_1_19();
        }
    }

    public String getLocalizedName() {
        return this.localizedName;
    }
}
