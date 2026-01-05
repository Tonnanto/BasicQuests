package de.stamme.basicquests.model.wrapper.structure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.structure.Structure;
import org.bukkit.util.StructureSearchResult;
import org.jetbrains.annotations.Nullable;

// Only compatible with spigot 1.19+
public class QuestStructureService_1_21 extends QuestStructureService {

  @Override
  public @Nullable Location findStructureNearLocation(
      QuestStructureType structureType, Location nearLocation, World world) {

    Location nearestLocation = null;
    double nearestDistance = 9999;
    List<Structure> matchingStructures = toSpigotStructure(structureType);
    if (matchingStructures == null) return null;

    for (Structure structure : matchingStructures) {
      if (structure == null) continue;

      StructureSearchResult searchResult =
          world.locateNearestStructure(nearLocation, structure, 3, false);
      if (searchResult == null) continue;

      Location location = searchResult.getLocation();

      double distance =
          Math.sqrt(
              Math.pow(Math.abs(nearLocation.getX() - location.getX()), 2)
                  + Math.pow(Math.abs(nearLocation.getZ() - location.getZ()), 2));
      if (distance < nearestDistance) {
        nearestDistance = distance;
        nearestLocation = location;
      }
    }

    return nearestLocation;
  }

  @Nullable
  private List<Structure> toSpigotStructure(QuestStructureType questStructureType) {
    switch (questStructureType) {
      case VILLAGE:
        return Arrays.asList(
            Structure.VILLAGE_PLAINS,
            Structure.VILLAGE_SAVANNA,
            Structure.VILLAGE_DESERT,
            Structure.VILLAGE_SNOWY,
            Structure.VILLAGE_TAIGA);
      case MINESHAFT:
        return Arrays.asList(Structure.MINESHAFT, Structure.MINESHAFT_MESA);
      case FORTRESS:
        return Collections.singletonList(Structure.FORTRESS);
      case STRONGHOLD:
        return Collections.singletonList(Structure.STRONGHOLD);
      case JUNGLE_PYRAMID:
        return Collections.singletonList(Structure.JUNGLE_PYRAMID);
      case OCEAN_RUIN:
        return Arrays.asList(Structure.OCEAN_RUIN_WARM, Structure.OCEAN_RUIN_COLD);
      case DESERT_PYRAMID:
        return Collections.singletonList(Structure.DESERT_PYRAMID);
      case IGLOO:
        return Collections.singletonList(Structure.IGLOO);
      case SWAMP_HUT:
        return Collections.singletonList(Structure.SWAMP_HUT);
      case MONUMENT:
        return Collections.singletonList(Structure.MONUMENT);
      case END_CITY:
        return Collections.singletonList(Structure.END_CITY);
      case MANSION:
        return Collections.singletonList(Structure.MANSION);
      case BURIED_TREASURE:
        return Collections.singletonList(Structure.BURIED_TREASURE);
      case SHIPWRECK:
        return Arrays.asList(Structure.SHIPWRECK, Structure.SHIPWRECK_BEACHED);
      case PILLAGER_OUTPOST:
        return Collections.singletonList(Structure.PILLAGER_OUTPOST);
      case RUINED_PORTAL:
        return Arrays.asList(
            Structure.RUINED_PORTAL,
            Structure.RUINED_PORTAL_DESERT,
            Structure.RUINED_PORTAL_JUNGLE,
            Structure.RUINED_PORTAL_MOUNTAIN,
            Structure.RUINED_PORTAL_NETHER,
            Structure.RUINED_PORTAL_OCEAN,
            Structure.RUINED_PORTAL_SWAMP);

      case BASTION_REMNANT:
        return Collections.singletonList(Structure.BASTION_REMNANT);
      case ANCIENT_CITY:
        return Collections.singletonList(Structure.ANCIENT_CITY);
      case TRAIL_RUINS:
        return Collections.singletonList(Structure.TRAIL_RUINS);
      default:
        return null;
    }
  }
}
