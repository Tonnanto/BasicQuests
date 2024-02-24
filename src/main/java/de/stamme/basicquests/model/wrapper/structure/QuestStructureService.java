package de.stamme.basicquests.model.wrapper.structure;

import de.stamme.basicquests.BasicQuestsPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public abstract class QuestStructureService {

    private static QuestStructureService instance;

    /**
     * @return the QuestStructureService that handles Structures correctly for the current spigot version of the server.
     */
    public static QuestStructureService getInstance() {
        if (instance == null) {
            switch (BasicQuestsPlugin.getBukkitVersion()) {
                case v1_16:
                case v1_17:
                    instance = new QuestStructureService_1_16();
                    break;
                case v1_18:
                    instance = new QuestStructureService_1_18();
                    break;
                case v1_19:
                case v1_20:
                default:
                    instance = new QuestStructureService_1_19();
                    break;
            }
        }
        return instance;
    }


    @Nullable
    abstract Location findStructureNearLocation(QuestStructureType structureType, Location nearLocation, World world);

    /**
     * @param structureName the name of the structure
     * @return the QuestStructureType for the given name
     */
    @Nullable
    public QuestStructureType fromString(String structureName) {
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

            case "TRAIL_RUINS": // 1.20 +
                return QuestStructureType.TRAIL_RUINS;

            default:
                return null;
        }
    }
}
