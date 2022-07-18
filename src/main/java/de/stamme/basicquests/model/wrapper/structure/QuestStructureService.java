package de.stamme.basicquests.model.wrapper.structure;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public interface QuestStructureService {
    @Nullable Location findStructureNearLocation(QuestStructureType structureType, Location nearLocation, World world);
}
