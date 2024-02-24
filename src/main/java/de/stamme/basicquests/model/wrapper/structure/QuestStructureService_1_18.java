package de.stamme.basicquests.model.wrapper.structure;

import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

// In 1.18 only VILLAGE, RUINED_PORTAL, SHIPWRECK, and OCEAN RUINS can be located...
public class QuestStructureService_1_18 extends QuestStructureService {

    @Override
    public @Nullable Location findStructureNearLocation(QuestStructureType questStructureType, Location nearLocation, World world) {
        Location nearestLocation = null;
        double nearestDistance = 9999;
        List<StructureType> matchingStructures = this.toSpigotStructure(questStructureType);
        if (matchingStructures == null) return null;

        for (StructureType structureType : matchingStructures) {
            if (structureType == null) continue;

            Location location = world.locateNearestStructure(nearLocation, structureType, 3, false);

            if (location == null) continue;

            double distance = Math.sqrt(Math.abs(nearLocation.getX() - location.getX()) + Math.abs(nearLocation.getZ() - location.getZ()));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestLocation = location;
            }
        }

        assert nearestLocation != null;

        return nearestLocation;
    }

    @Nullable
    private List<StructureType> toSpigotStructure(QuestStructureType questStructureType) {
        switch (questStructureType) {
            case VILLAGE:
                return Collections.singletonList(StructureType.VILLAGE);
            case MINESHAFT:
                return Collections.singletonList(StructureType.MINESHAFT);
            case FORTRESS:
                return Collections.singletonList(StructureType.NETHER_FORTRESS);
            case STRONGHOLD:
                return Collections.singletonList(StructureType.STRONGHOLD);
            case JUNGLE_PYRAMID:
                return Collections.singletonList(StructureType.JUNGLE_PYRAMID);
            case OCEAN_RUIN:
                return Collections.singletonList(StructureType.OCEAN_RUIN);
            case DESERT_PYRAMID:
                return Collections.singletonList(StructureType.DESERT_PYRAMID);
            case IGLOO:
                return Collections.singletonList(StructureType.IGLOO);
            case SWAMP_HUT:
                return Collections.singletonList(StructureType.SWAMP_HUT);
            case MONUMENT:
                return Collections.singletonList(StructureType.OCEAN_MONUMENT);
            case END_CITY:
                return Collections.singletonList(StructureType.END_CITY);
            case MANSION:
                return Collections.singletonList(StructureType.WOODLAND_MANSION);
            case BURIED_TREASURE:
                return Collections.singletonList(StructureType.BURIED_TREASURE);
            case SHIPWRECK:
                return Collections.singletonList(StructureType.SHIPWRECK);
            case PILLAGER_OUTPOST:
                return Collections.singletonList(StructureType.PILLAGER_OUTPOST);
            case RUINED_PORTAL:
                return Collections.singletonList(StructureType.RUINED_PORTAL);
            case BASTION_REMNANT:
                return Collections.singletonList(StructureType.BASTION_REMNANT);
            default:
                return null;
        }
    }

    /**
     * In 1.18 only a handful of structures can be found. Therefore, only quests with these structures can be generated.
     */
    @Override
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

            case "OCEAN_RUIN": // bukkit 1.16
            case "OCEAN_RUIN_WARM":
            case "OCEAN_RUIN_COLD":
                // Not differentiating between different ocean ruins.
                return QuestStructureType.OCEAN_RUIN;

            case "SHIPWRECK":
            case "SHIPWRECK_BEACHED":
                return QuestStructureType.SHIPWRECK;

            case "RUINED_PORTAL":
            case "RUINED_PORTAL_DESERT":
            case "RUINED_PORTAL_JUNGLE":
            case "RUINED_PORTAL_MOUNTAIN":
            case "RUINED_PORTAL_NETHER":
            case "RUINED_PORTAL_OCEAN":
            case "RUINED_PORTAL_SWAMP":
                // Not differentiating between different ruined portals.
                return QuestStructureType.RUINED_PORTAL;
            default:
                return null;
        }
    }
}
