package de.stamme.basicquests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.stamme.basicquests.model.quests.ChopWoodQuest;
import de.stamme.basicquests.model.quests.Quest;
import de.stamme.basicquests.model.rewards.Reward;
import org.bukkit.Material;

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
    when(quest.getOptionNames()).thenCallRealMethod();
    return quest;
  }

  static Reward getReward() {
    return new Reward();
  }
}
