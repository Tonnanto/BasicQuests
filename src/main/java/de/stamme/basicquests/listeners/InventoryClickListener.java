package de.stamme.basicquests.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		
		InventoryAction action = event.getAction();
		Player player = (Player) event.getWhoClicked();
		
		if (player != null) {
			
//			Main.log(event.getAction().name());
			
//			switch (action) {
//			case PICKUP_ALL :
				
				//
//				ItemStack itemStack = event.getCurrentItem();
//				Main.log("" + itemStack.getAmount());
//				Main.log(itemStack.getType().toString());
				
//			}
		}
		
		
	}
	
}
