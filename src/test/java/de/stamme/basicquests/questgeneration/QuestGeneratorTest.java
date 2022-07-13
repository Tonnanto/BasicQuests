package de.stamme.basicquests.questgeneration;

import de.stamme.basicquests.MockPlayers;
import de.stamme.basicquests.MockServer;
import de.stamme.basicquests.data.GenerationFileService;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quests.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;


public class QuestGeneratorTest {

    QuestPlayer questPlayer1;
    static QuestGenerator questGenerator;


    @BeforeAll
    static void setUp() {
        MockServer.init();
        questGenerator = QuestGenerator.getInstance();
    }

    @BeforeEach
    public void setUpCommand() {
        // Mock Player
        Player player1 = MockPlayers.getPlayer();
        QuestPlayer questPlayer1 = MockPlayers.getQuestPlayer(player1, 0);
        Main.getPlugin().getQuestPlayers().put(player1.getUniqueId(), questPlayer1);

        this.questPlayer1 = questPlayer1;
    }

    @Test
    public void canQuestsBeGenerated() throws QuestGenerationException {

        List<Quest> generatedQuests = new ArrayList<>();
        Map<QuestType, Integer> questTypeMap = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            Quest quest = questGenerator.generate(questPlayer1);
            generatedQuests.add(quest);

            if (questTypeMap.containsKey(quest.getQuestType())) {
                questTypeMap.put(quest.getQuestType(), questTypeMap.get(quest.getQuestType()) + 1);
            } else {
                questTypeMap.put(quest.getQuestType(), 1);
            }

            System.out.println(quest.getInfo(true));
        }

        checkQuests(generatedQuests);

        System.out.println(questTypeMap);
    }

    @Test
    public void canAllQuestTypesBeGenerated() throws QuestGenerationException {
        List<Quest> quests = new ArrayList<>();
        quests.add(QuestGenerator.getInstance().generateChopWoodQuest(questPlayer1, 1, 1));
        quests.add(QuestGenerator.getInstance().generateBreakBlockQuest(questPlayer1, 1, 1));
        quests.add(QuestGenerator.getInstance().generateEnchantItemQuest(questPlayer1, 1, 1));
        quests.add(QuestGenerator.getInstance().generateFindStructureQuest(questPlayer1, 1));
        quests.add(QuestGenerator.getInstance().generateGainLevelQuest(questPlayer1, 1, 1));
        quests.add(QuestGenerator.getInstance().generateHarvestBlockQuest(questPlayer1, 1, 1));
        quests.add(QuestGenerator.getInstance().generateKillEntityQuest(questPlayer1, 1, 1));
        quests.add(QuestGenerator.getInstance().generateMineBlockQuest(questPlayer1, 1, 1));
        quests.add(QuestGenerator.getInstance().generateReachLevelQuest(questPlayer1, 1));
        checkQuests(quests);
    }


    public void checkQuests(List<Quest> quests) {

        for (Quest quest: quests) {
            // has reward?
            Assertions.assertNotNull(quest.getReward());
            List<ItemStack> itemReward = quest.getReward().getItems();
            int xpReward = quest.getReward().getXp();
            BigDecimal moneyReward = quest.getReward().getMoney();
            boolean hasReward = (itemReward != null && !itemReward.isEmpty()) || xpReward > 0 || moneyReward.compareTo(BigDecimal.ZERO) > 0;
            Assertions.assertTrue(hasReward);

            // valid amount
            GenerationConfig questTypeConfig =  GenerationFileService.getInstance().getConfigForQuestType(quest.getQuestType());
            if (questTypeConfig.getOptions() != null && !questTypeConfig.getOptions().isEmpty()) {

                // find GenerationOption
                String materialName;
                switch (quest.getQuestType()) {
                    case BREAK_BLOCK:
                        assert quest instanceof BlockBreakQuest;
                        materialName = ((BlockBreakQuest) quest).getMaterial().name();
                        break;
                    case MINE_BLOCK:
                        assert quest instanceof MineBlockQuest;
                        materialName = ((MineBlockQuest) quest).getMaterial().name();
                        break;
                    case HARVEST_BLOCK:
                        assert quest instanceof HarvestBlockQuest;
                        materialName = ((HarvestBlockQuest) quest).getMaterial().name();
                        break;
                    case ENCHANT_ITEM:
                        assert quest instanceof EnchantItemQuest;
                        materialName = ((EnchantItemQuest) quest).getMaterial().name();
                        break;
                    case KILL_ENTITY:
                        assert quest instanceof EntityKillQuest;
                        materialName = ((EntityKillQuest) quest).getEntity().name();
                        break;
                    case FIND_STRUCTURE:
                        assert quest instanceof FindStructureQuest;
                        materialName = ((FindStructureQuest) quest).getStructure().getName().toUpperCase();
                        break;
                    case CHOP_WOOD:
                        assert quest instanceof ChopWoodQuest;
                        ChopWoodQuest chopWoodQuest = (ChopWoodQuest) quest;
                        if (chopWoodQuest.getMaterialString() != null && !chopWoodQuest.getMaterialString().isEmpty())
                            materialName = chopWoodQuest.getMaterialString();
                        else
                            materialName = ((ChopWoodQuest) quest).getMaterial().name();
                        break;
                    default:
                        materialName = "";
                }
                String finalMaterialName = materialName;
                Optional<GenerationOption> generationOption = questTypeConfig.getOptions().stream().filter(option -> option.getName().equals(finalMaterialName)).findFirst();
                Assertions.assertTrue(generationOption.isPresent());

                int minAmount = (generationOption.get().getMin() == 0) ? questTypeConfig.getDefault_min() : generationOption.get().getMin();
                int maxAmount = (generationOption.get().getMax() == 0) ? questTypeConfig.getDefault_max() : generationOption.get().getMax();
                int step = (generationOption.get().getStep() == 0) ? questTypeConfig.getDefault_step() : generationOption.get().getStep();

                maxAmount = Math.max(maxAmount, 1);
                step = Math.max(step, 1);

                // Check if amount is valid
                Assertions.assertTrue(quest.getGoal() >= minAmount);
                Assertions.assertTrue(quest.getGoal() <= maxAmount);
                Assertions.assertEquals(0, quest.getGoal() % step);
            }
        }
    }
}
