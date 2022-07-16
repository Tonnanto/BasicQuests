package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.wrapper.QuestStructureType;
import de.stamme.basicquests.util.StringFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.generator.structure.Structure;
import org.bukkit.util.StructureSearchResult;

import java.util.Map.Entry;
import java.util.UUID;

public class FindStructureQuest extends Quest {


	// ---------------------------------------------------------------------------------------
	// Quest State
	// ---------------------------------------------------------------------------------------

	private final QuestStructureType structure;
	private final double radius;


	// ---------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------

	public FindStructureQuest(QuestStructureType structure, double radius, int goal, Reward reward) {
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
		Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
			for (Entry<UUID, QuestPlayer> entry: Main.getPlugin().getQuestPlayers().entrySet()) {
				for (Quest quest: entry.getValue().getQuests()) {
					if (quest instanceof FindStructureQuest && !quest.isCompleted()) {
						QuestPlayer questPlayer = entry.getValue();
						if (questPlayer != null) {

							FindStructureQuest fsq = (FindStructureQuest) quest;
							Structure spigotStructure = fsq.structure.toSpigotStructure();
							if (spigotStructure == null) continue;

							Location playerLoc = questPlayer.getPlayer().getLocation();
							StructureSearchResult structureSearchResult = questPlayer.getPlayer().getWorld().locateNearestStructure(
									playerLoc, spigotStructure, 100, false
							);

							if (structureSearchResult == null) continue;
							Location nearestStructureLoc = structureSearchResult.getLocation();

							if (Math.abs(playerLoc.getX() - nearestStructureLoc.getX()) < fsq.radius && Math.abs(playerLoc.getZ() - nearestStructureLoc.getZ()) < fsq.radius) {
								fsq.progress(1, questPlayer);
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
		data.setStructure(structure.name().toLowerCase());
		data.setRadius(radius);
		return data;
	}


	// ---------------------------------------------------------------------------------------
	// Getter & Setter
	// ---------------------------------------------------------------------------------------

	@Override
	public String getName() {
		return String.format("Find a %s", StringFormatter.format(structure.name()));
	}

	@Override
	public String[] getDecisionObjectNames() {
		return new String[]{QuestType.FIND_STRUCTURE.name(), structure.name()};
	}

	@Override
	public final QuestType getQuestType() {
		return QuestType.FIND_STRUCTURE;
	}

	public QuestStructureType getStructure() {
		return structure;
	}

	public double getRadius() {
		return radius;
	}
}
