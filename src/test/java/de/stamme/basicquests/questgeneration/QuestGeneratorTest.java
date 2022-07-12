package de.stamme.basicquests.questgeneration;

import de.stamme.basicquests.MockPlayers;
import de.stamme.basicquests.MockServer;
import de.stamme.basicquests.data.GenerationFileService;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.Quest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;

import static org.mockito.Mockito.*;


public class QuestGeneratorTest {

    QuestPlayer questPlayer1;
    static QuestGenerator questGenerator;

    @BeforeAll
    static void setUp() {
        MockServer.init();
        questGenerator = QuestGenerator.getInstance();//mock(QuestGenerator.class);
//        when(questGenerator.generate(any(QuestPlayer.class))).thenCallRealMethod();
//        when(questGenerator.decide(anyList())).thenCallRealMethod();
//        when(questGenerator.decide(anyList(), any(QuestPlayer.class))).thenCallRealMethod();
//        when(questGenerator.generateAmount(anyInt(), anyInt(), anyInt(), anyDouble())).thenCallRealMethod();
//        when(questGenerator.generateAmount(any(DecisionObject.class), any(), anyDouble())).thenCallRealMethod();
//        when(questGenerator.generateChopWoodQuest(any(QuestPlayer.class), anyDouble(), anyDouble())).thenCallRealMethod();
//        when(questGenerator.generateBreakBlockQuest(any(QuestPlayer.class), anyDouble(), anyDouble())).thenCallRealMethod();
//        when(questGenerator.generateEnchantItemQuest(any(QuestPlayer.class), anyDouble(), anyDouble())).thenCallRealMethod();
//        when(questGenerator.generateFindStructureQuest(any(QuestPlayer.class), anyDouble())).thenCallRealMethod();
//        when(questGenerator.generateGainLevelQuest(any(QuestPlayer.class), anyDouble(), anyDouble())).thenCallRealMethod();
//        when(questGenerator.generateHarvestBlockQuest(any(QuestPlayer.class), anyDouble(), anyDouble())).thenCallRealMethod();
//        when(questGenerator.generateKillEntityQuest(any(QuestPlayer.class), anyDouble(), anyDouble())).thenCallRealMethod();
//        when(questGenerator.generateMineBlockQuest(any(QuestPlayer.class), anyDouble(), anyDouble())).thenCallRealMethod();
//        when(questGenerator.generateReachLevelQuest(any(QuestPlayer.class), anyDouble())).thenCallRealMethod();
//        when(questGenerator.getPlaytimeAmountFactor(any(Player.class))).thenCallRealMethod();


        File file = new File("src/main/resources/quest_generation.yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        GenerationFileService generationFileService = mock(GenerationFileService.class);
        when(generationFileService.getConfiguration()).thenReturn(configuration);
        when(generationFileService.getQuestTypeGenerationConfig()).thenCallRealMethod();
        when(generationFileService.getChopWoodGenerationConfig()).thenCallRealMethod();
        when(generationFileService.getMineBlockGenerationConfig()).thenCallRealMethod();

        MockedStatic<GenerationFileService> mockedStaticGenerationFileService = mockStatic(GenerationFileService.class);
        mockedStaticGenerationFileService.when(GenerationFileService::getInstance).thenReturn(generationFileService);
    }

    @BeforeEach
    public void setUpCommand() {
        // Mock Player
        Player player1 = MockPlayers.getPlayer();
        QuestPlayer questPlayer1 = MockPlayers.getQuestPlayer(player1, 0);
        Main.getPlugin().getQuestPlayers().put(player1.getUniqueId(), questPlayer1);

//        Player player2 = MockPlayers.getPlayer();
//        QuestPlayer questPlayer2 = MockPlayers.getQuestPlayer(player2);
//        Main.getPlugin().getQuestPlayers().put(player2.getUniqueId(), questPlayer2);

        this.questPlayer1 = questPlayer1;
    }

    @Test
    public void canQuestsBeGenerated() throws QuestGenerationException {

        for (int i = 0; i < 100; i++) {
            Quest quest = questGenerator.generate(questPlayer1);
            System.out.println(quest.getInfo(true));
        }
    }
}
