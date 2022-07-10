package de.stamme.basicquests;

import de.stamme.basicquests.quests.ChopWoodQuest;
import de.stamme.basicquests.quests.Quest;
import de.stamme.basicquests.quests.Reward;
import org.bukkit.Material;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockQuests {

    static Quest getQuest() {
        return getChopWoodQuest(); // TODO Random quest type
    }

    static ChopWoodQuest getChopWoodQuest() {
        ChopWoodQuest quest = mock(ChopWoodQuest.class);
        when(quest.getGoal()).thenReturn(32);
        when(quest.getMaterialString()).thenReturn("LOG");
        when(quest.getMaterial()).thenReturn(Material.OAK_LOG);
        when(quest.getReward()).thenReturn(getReward());
        when(quest.getDecisionObjectNames()).thenCallRealMethod();
        return quest;
    }

    static Reward getReward() {
        return new Reward();
    }

}
