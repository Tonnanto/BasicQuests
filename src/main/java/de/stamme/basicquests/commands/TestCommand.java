package de.stamme.basicquests.commands;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			World world = player.getWorld();
			
			Location nearest_village_loc = world.locateNearestStructure(player.getLocation(), StructureType.NETHER_FORTRESS, 100, false);
			assert nearest_village_loc != null;

			player.sendMessage(String.format("x: %s y: %s z: %s", nearest_village_loc.getX(), nearest_village_loc.getY(), nearest_village_loc.getZ()));
		}
		
		return false;
	}

}
