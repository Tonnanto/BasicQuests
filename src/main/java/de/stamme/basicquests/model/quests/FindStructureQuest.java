package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.wrapper.structure.QuestStructureType;
import de.stamme.basicquests.config.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.text.MessageFormat;
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
		Bukkit.getScheduler().runTaskTimer(BasicQuestsPlugin.getPlugin(), () -> {
			for (Entry<UUID, QuestPlayer> entry: BasicQuestsPlugin.getPlugin().getQuestPlayers().entrySet()) {
				for (Quest quest: entry.getValue().getQuests()) {
					if (quest instanceof FindStructureQuest && !quest.isCompleted()) {
						QuestPlayer questPlayer = entry.getValue();
						if (questPlayer != null) {

							FindStructureQuest fsq = (FindStructureQuest) quest;

							Location playerLoc = questPlayer.getPlayer().getLocation();
							World playerWorld = questPlayer.getPlayer().getWorld();
							Location nearestStructureLoc = fsq.getStructure().findNearLocation(playerLoc, playerWorld);

							if (nearestStructureLoc == null) continue;

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
		return MessageFormat.format(MessagesConfig.getMessage("quest.findStructure.generic"), this.structure.getLocalizedName());
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

	@Override
	public String getOptionKey() {
		return structure.name();
	}
}
