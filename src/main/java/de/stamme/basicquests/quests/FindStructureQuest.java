package de.stamme.basicquests.quests;

import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.main.StringFormatter;

public class FindStructureQuest extends Quest {
	
	private final StructureType structure;
	private final double radius;

	public FindStructureQuest(StructureType structure, double radius, int goal, Reward reward) {
		super(goal, reward);
		this.structure = structure;
		this.radius = radius;
	}

	@Override
	public String getName() {
		return String.format("Find a %s", StringFormatter.format(structure.getName()));
	}
	
	public QuestData toData() {
		QuestData data = super.toData();
		
		data.questType = QuestType.FIND_STRUCTURE.name();
		data.structure = structure.getName().toLowerCase();
		data.radius = radius;

		return data;
	}
	
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.FIND_STRUCTURE.name(), structure.getName()};
	}
	
	// Looks for active FindStructureQeusts and completes them if a player found the structure
	public static void startScheduler() {
		
		Bukkit.getScheduler().runTaskTimer(Main.plugin, () -> {
			for (Entry<UUID, QuestPlayer> entry: Main.plugin.questPlayer.entrySet()) {
				for (Quest quest: entry.getValue().quests) {
					if (quest instanceof FindStructureQuest && !quest.completed()) {
						QuestPlayer player = entry.getValue();
						if (player != null) {

							FindStructureQuest fsq = (FindStructureQuest) quest;
							Location playerLoc = player.player.getLocation();
							Location nearest_structure_loc = player.player.getWorld().locateNearestStructure(playerLoc, fsq.structure, 100, false);

							if (nearest_structure_loc != null) {
								if (Math.abs(playerLoc.getX() - nearest_structure_loc.getX()) < fsq.radius && Math.abs(playerLoc.getZ() - nearest_structure_loc.getZ()) < fsq.radius) {
									fsq.progress(1, player);
								}
							}
						}
					}
				}
			}
		}, 40L, 40L);
	}
}
