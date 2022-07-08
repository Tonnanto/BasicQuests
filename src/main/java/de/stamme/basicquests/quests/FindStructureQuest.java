package de.stamme.basicquests.quests;

import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.util.StringFormatter;

public class FindStructureQuest extends Quest {


	// ---------------------------------------------------------------------------------------
	// Quest State
	// ---------------------------------------------------------------------------------------

	private final StructureType structure;
	private final double radius;


	// ---------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------

	public FindStructureQuest(StructureType structure, double radius, int goal, Reward reward) {
		super(goal, reward);
		this.structure = structure;
		this.radius = radius;
	}


	// ---------------------------------------------------------------------------------------
	// Functionality
	// ---------------------------------------------------------------------------------------

	/**
	 * Looks for active FindStructureQuests and completes them if a player found the structure
	 */
	public static void startScheduler() {
		Bukkit.getScheduler().runTaskTimer(Main.plugin, () -> {
			for (Entry<UUID, QuestPlayer> entry: Main.plugin.questPlayer.entrySet()) {
				for (Quest quest: entry.getValue().getQuests()) {
					if (quest instanceof FindStructureQuest && !quest.isCompleted()) {
						QuestPlayer questPlayer = entry.getValue();
						if (questPlayer != null) {

							FindStructureQuest fsq = (FindStructureQuest) quest;
							Location playerLoc = questPlayer.getPlayer().getLocation();
							Location nearest_structure_loc = questPlayer.getPlayer().getWorld().locateNearestStructure(playerLoc, fsq.structure, 100, false);

							if (nearest_structure_loc != null) {
								if (Math.abs(playerLoc.getX() - nearest_structure_loc.getX()) < fsq.radius && Math.abs(playerLoc.getZ() - nearest_structure_loc.getZ()) < fsq.radius) {
									fsq.progress(1, questPlayer);
								}
							}
						}
					}
				}
			}
		}, 40L, 40L);
	}

	@Override
	public QuestData toData() {
		QuestData data = super.toData();
		data.setQuestType(QuestType.FIND_STRUCTURE.name());
		data.setStructure(structure.getName().toLowerCase());
		data.setRadius(radius);
		return data;
	}


	// ---------------------------------------------------------------------------------------
	// Getter & Setter
	// ---------------------------------------------------------------------------------------

	@Override
	public String getName() {
		return String.format("Find a %s", StringFormatter.format(structure.getName()));
	}

	@Override
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.FIND_STRUCTURE.name(), structure.getName()};
	}
}
