package de.stamme.basicquests.questgeneration;

import de.stamme.basicquests.MockPlayers;
import de.stamme.basicquests.MockServer;
import de.stamme.basicquests.model.generation.GenerationConfig;
import de.stamme.basicquests.model.generation.GenerationOption;
import de.stamme.basicquests.model.generation.QuestGenerationException;
import de.stamme.basicquests.util.GenerationFileService;
import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.quests.*;
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

    static private final Map<QuestType, Map<GenerationOption, Integer>> optionCounterForQuestType = new HashMap<>();


    @BeforeAll
    static void setUp() {
        MockServer.init();
        questGenerator = QuestGenerator.getInstance();

        // Add all quest types to optionCounterForQuestType
        Arrays.stream(QuestType.values()).forEach(questType -> {
            Map<GenerationOption, Integer> materialMap = new HashMap<>();
            GenerationConfig questTypeConfig =  GenerationFileService.getInstance().getConfigForQuestType(questType);
            if (questTypeConfig.getOptions() != null && !questTypeConfig.getOptions().isEmpty()) {
                questTypeConfig.getOptions().forEach(generationOption -> materialMap.put(generationOption, 0));
            }
            optionCounterForQuestType.put(questType, materialMap);
        });
    }

    @BeforeEach
    public void setUpCommand() {
        // Mock Player
        Player player1 = MockPlayers.getPlayer();
        QuestPlayer questPlayer1 = MockPlayers.getQuestPlayer(player1, 0);
        BasicQuestsPlugin.getPlugin().getQuestPlayers().put(player1.getUniqueId(), questPlayer1);

        this.questPlayer1 = questPlayer1;
    }

    @Test
    public void canQuestsBeGenerated() throws QuestGenerationException {

        List<Quest> generatedQuests = new ArrayList<>();
        Map<QuestType, Integer> questTypeMap = new HashMap<>();

        for (int i = 0; i < 1000; i++) {
            Quest quest = questGenerator.generate(questPlayer1);
            generatedQuests.add(quest);

            if (questTypeMap.containsKey(quest.getQuestType())) {
                questTypeMap.put(quest.getQuestType(), questTypeMap.get(quest.getQuestType()) + 1);
            } else {
                questTypeMap.put(quest.getQuestType(), 1);
            }

//            System.out.println(quest.getInfo(true));
        }

        checkQuests(generatedQuests);

        validateAndDisplayMaterialCountsForQuestType();
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
        quests.add(QuestGenerator.getInstance().generateVillagerTradeQuest(questPlayer1, 1, 1));
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
                String materialName = getMaterialNameForQuest(quest);
                increaseMaterialForQuestType(materialName, quest.getQuestType());
                Optional<GenerationOption> generationOption = questTypeConfig.getOptions().stream().filter(option -> option.getName().equals(materialName)).findFirst();
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

                // Check if all materials have been generated
            }
        }
    }

    public String getMaterialNameForQuest(Quest quest) {
        switch (quest.getQuestType()) {
            case BREAK_BLOCK:
                assert quest instanceof BlockBreakQuest;
                return ((BlockBreakQuest) quest).getMaterial().name();
            case MINE_BLOCK:
                assert quest instanceof MineBlockQuest;
                return ((MineBlockQuest) quest).getMaterial().name();
            case HARVEST_BLOCK:
                assert quest instanceof HarvestBlockQuest;
                return ((HarvestBlockQuest) quest).getMaterial().name();
            case ENCHANT_ITEM:
                assert quest instanceof EnchantItemQuest;
                return ((EnchantItemQuest) quest).getMaterial().name();
            case KILL_ENTITY:
                assert quest instanceof EntityKillQuest;
                return ((EntityKillQuest) quest).getEntity().name();
            case FIND_STRUCTURE:
                assert quest instanceof FindStructureQuest;
                return ((FindStructureQuest) quest).getStructure().name().toUpperCase();
            case CHOP_WOOD:
                assert quest instanceof ChopWoodQuest;
                ChopWoodQuest chopWoodQuest = (ChopWoodQuest) quest;
                if (chopWoodQuest.getMaterialString() != null && !chopWoodQuest.getMaterialString().isEmpty())
                    return chopWoodQuest.getMaterialString();
                else
                    return ((ChopWoodQuest) quest).getMaterial().name();
            case VILLAGER_TRADE:
                assert quest instanceof VillagerTradeQuest;
                return ((VillagerTradeQuest) quest).getProfession().getKey().getKey().toUpperCase();
            default:
                return "";
        }
    }

    public void increaseMaterialForQuestType(String materialString, QuestType questType) {

        Map<GenerationOption, Integer> materialCounterMap = optionCounterForQuestType.get(questType);

        Optional<GenerationOption> materialOption = materialCounterMap.keySet().stream().filter(generationOption -> generationOption.getName().equals(materialString)).findFirst();
        assert materialOption.isPresent();

        materialCounterMap.put(materialOption.get(), materialCounterMap.get(materialOption.get()) + 1);
    }

    private void validateAndDisplayMaterialCountsForQuestType() {
        StringBuilder sb = new StringBuilder("\n\nMaterial counts for Quest Type:");

        for (Map.Entry<QuestType, Map<GenerationOption, Integer>> materialMapEntry: optionCounterForQuestType.entrySet()) {
            sb.append("\n\nQuest Type: ").append(materialMapEntry.getKey().name());
            Optional<Integer> questTypeCount = materialMapEntry.getValue().values().stream().reduce(Integer::sum);

            for (Map.Entry<GenerationOption, Integer> materialEntry: materialMapEntry.getValue().entrySet()) {
                double actualWeight = (double) materialEntry.getValue() / questTypeCount.get();

                String materialCountString = String.format("%-20s count: %-5s        %5.2f %%    (%-5.2f%% expected)", materialEntry.getKey().getName(),  materialEntry.getValue(), actualWeight * 100, materialEntry.getKey().getWeight() * 100);
                sb.append("\n- ").append(materialCountString);

//                Assertions.assertTrue(Math.abs(materialEntry.getKey().getWeight() - actualWeight) < 0.1);
            }
        }

        System.out.println(sb);
    }
}
