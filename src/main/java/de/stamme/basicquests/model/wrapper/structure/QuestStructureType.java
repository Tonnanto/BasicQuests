package de.stamme.basicquests.model.wrapper.structure;


import de.stamme.basicquests.Main;
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



    /**
     * @param structureName the name of the structure
     * @return the spigot 1.19 equivalent of the Structure
     */
    @Nullable
    public static QuestStructureType fromString(String structureName) {
        switch (structureName.toUpperCase()) {
            case "VILLAGE":
            case "VILLAGE_PLAINS":
            case "VILLAGE_DESERT":
            case "VILLAGE_SAVANNA":
            case "VILLAGE_SNOWY":
            case "VILLAGE_TAIGA":
                // Not differentiating between different villages.
                return QuestStructureType.VILLAGE;

            case "MINESHAFT":
            case "MINESHAFT_MESA":
                // Not differentiating between different mine shafts.
                return QuestStructureType.MINESHAFT;

            case "FORTRESS":
                return QuestStructureType.FORTRESS;

            case "STRONGHOLD":
                return QuestStructureType.STRONGHOLD;

            case "JUNGLE_PYRAMID":
                return QuestStructureType.JUNGLE_PYRAMID;

            case "OCEAN_RUIN": // bukkit 1.16
            case "OCEAN_RUIN_WARM":
            case "OCEAN_RUIN_COLD":
                // Not differentiating between different ocean ruins.
                return QuestStructureType.OCEAN_RUIN;

            case "DESERT_PYRAMID":
                return QuestStructureType.DESERT_PYRAMID;

            case "IGLOO":
                return QuestStructureType.IGLOO;

            case "SWAMP_HUT":
                return QuestStructureType.SWAMP_HUT;

            case "MONUMENT":
                return QuestStructureType.MONUMENT;

            case "ENDCITY": // bukkit 1.16
            case "END_CITY":
                return QuestStructureType.END_CITY;

            case "MANSION":
                return QuestStructureType.MANSION;

            case "BURIED_TREASURE":
                return QuestStructureType.BURIED_TREASURE;

            case "SHIPWRECK":
            case "SHIPWRECK_BEACHED":
                return QuestStructureType.SHIPWRECK;

            case "PILLAGER_OUTPOST":
                return QuestStructureType.PILLAGER_OUTPOST;

            case "RUINED_PORTAL":
            case "RUINED_PORTAL_DESERT":
            case "RUINED_PORTAL_JUNGLE":
            case "RUINED_PORTAL_MOUNTAIN":
            case "RUINED_PORTAL_NETHER":
            case "RUINED_PORTAL_OCEAN":
            case "RUINED_PORTAL_SWAMP":
                // Not differentiating between different ruined portals.
                return QuestStructureType.RUINED_PORTAL;

            case "BASTION_REMNANT":
                return QuestStructureType.BASTION_REMNANT;

            case "ANCIENT_CITY": // 1.19 +
                return QuestStructureType.ANCIENT_CITY;

            default:
                return null;
        }
    }

    @Nullable
    public Location findNearLocation(Location nearLocation, World world) {
        return getQuestStructureService().findStructureNearLocation(this, nearLocation, world);
    }

    /**
     * @return the QuestStructureService that handles Structures correctly for the current spigot version of the server.
     */
    private QuestStructureService getQuestStructureService() {
        switch (Main.getBukkitVersion()) {
            case v1_16:
            case v1_17:
            case v1_18:
                return new QuestStructureService_1_16();
            case v1_19:
            default:
                return new QuestStructureService_1_19();
        }
    }
}
