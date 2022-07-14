package de.stamme.basicquests.commands;

import de.stamme.basicquests.data.wrapper.QuestStructureType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			World world = player.getWorld();

			String structureName = args[0];
			assert QuestStructureType.fromString(structureName) != null;
			Structure structureType = QuestStructureType.fromString(structureName).toSpigotStructure();
			assert structureType != null;
			Location nearest_village_loc = Objects.requireNonNull(world.locateNearestStructure(player.getLocation(), structureType, 100, false)).getLocation();

			player.sendMessage(String.format("x: %s y: %s z: %s", nearest_village_loc.getX(), nearest_village_loc.getY(), nearest_village_loc.getZ()));
		}
		
		return false;
	}

}
