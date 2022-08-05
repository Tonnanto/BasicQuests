package de.stamme.basicquests.listeners;

import de.stamme.basicquests.BasicQuestsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

public class BlockPlaceListener implements Listener {

	@EventHandler
	public void onBlockPlace(@NotNull BlockPlaceEvent event) {
		event.getBlock().setMetadata("basicquests.placed", new FixedMetadataValue(BasicQuestsPlugin.getPlugin(), true));
	}
	
}
