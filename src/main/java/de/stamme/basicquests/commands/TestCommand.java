package de.stamme.basicquests.commands;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			World world = player.getWorld();
			
			Location nearest_village_loc = world.locateNearestStructure(player.getLocation(), StructureType.NETHER_FORTRESS, 100, false);
			
			
			Chunk chunk = (Chunk) world.getChunkAt(nearest_village_loc);
			
			player.sendMessage(String.format("x: %s y: %s z: %s", nearest_village_loc.getX(), nearest_village_loc.getY(), nearest_village_loc.getZ()));
		}
		
		return false;
	}

}
