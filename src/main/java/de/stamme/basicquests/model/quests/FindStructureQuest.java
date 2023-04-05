package de.stamme.basicquests.model.quests;

import de.stamme.basicquests.config.MessagesConfig;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.rewards.Reward;
import de.stamme.basicquests.model.wrapper.structure.QuestStructureType;
import org.bukkit.Location;
import org.bukkit.World;

import java.text.MessageFormat;

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
     * Periodically called to check if a structure has been found
     */
    public void checkForProgress(QuestPlayer questPlayer) {
        Location playerLoc = questPlayer.getPlayer().getLocation();
        World playerWorld = questPlayer.getPlayer().getWorld();
        Location nearestStructureLoc = getStructure().findNearLocation(playerLoc, playerWorld);

        if (nearestStructureLoc == null) return;

        if (Math.abs(playerLoc.getX() - nearestStructureLoc.getX()) < getRadius() && Math.abs(playerLoc.getZ() - nearestStructureLoc.getZ()) < getRadius()) {
            progress(1, questPlayer);
        }
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
        return MessageFormat.format(MessagesConfig.getMessage("quests.find-structure.generic"), this.structure.getLocalizedName());
    }

    @Override
    public String[] getOptionNames() {
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
