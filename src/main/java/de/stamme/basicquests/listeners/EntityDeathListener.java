package de.stamme.basicquests.listeners;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.EntityKillQuest;
import de.stamme.basicquests.quests.Quest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		
		if (entity.getKiller() != null) {
			
			if (Main.plugin.questPlayer.containsKey(entity.getKiller().getUniqueId())) {
				QuestPlayer player = Main.plugin.questPlayer.get(entity.getKiller().getUniqueId());
				
				for (Quest q: player.quests) {
					if (q instanceof EntityKillQuest) {
						EntityKillQuest ekq = (EntityKillQuest) q;
						
						if (ekq.entity == entity.getType()) {
							ekq.progress(1, player);
						}
					}
				}
			}
		}
	}
}
