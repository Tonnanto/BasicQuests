package de.stamme.basicquests.model.wrapper.structure;

import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

// Only compatible with spigot 1.16 - 1.18!
public class QuestStructureService_1_16 extends QuestStructureService {

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
}
