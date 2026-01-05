package de.stamme.basicquests.commands;

import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.generation.GenerationConfig;
import de.stamme.basicquests.model.generation.GenerationOption;
import de.stamme.basicquests.model.generation.QuestGenerationException;
import de.stamme.basicquests.model.quests.*;
import de.stamme.basicquests.questgeneration.QuestGenerator;
import de.stamme.basicquests.util.GenerationFileService;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * This command serves debugging and test purposes only. It is deactivated in release versions.
 * Activate by adding an instance of this command to BasicQuestsCommandRouter.COMMANDS.
 */
public class TestCommand extends BasicQuestsCommand {
  public TestCommand() {
    super("test");
  }

  @Override
  public final @NotNull String getPermission() {
    return "basicquests.admin";
  }

  @Override
  public void evaluate(
      @NotNull BasicQuestsPlugin plugin,
      @NotNull CommandSender sender,
      @NotNull String alias,
      @NotNull @Unmodifiable List<String> params) {

    if (!(sender instanceof Player)) {
      return;
    }

    // Command executed by player
    Player player = (Player) sender;
    @Nullable QuestPlayer questPlayer = BasicQuestsPlugin.getPlugin().getQuestPlayer(player);

    int amount = 2000;
    testQuestGeneration(plugin, sender, questPlayer, amount);
  }

  /** Used to keep track of occurrences of quest types during test run of quest generation */
  private final Map<QuestType, Map<GenerationOption, Integer>> optionCounterForQuestType =
      new HashMap<>();

  private void testQuestGeneration(
      @NotNull BasicQuestsPlugin plugin,
      @NotNull CommandSender sender,
      QuestPlayer player,
      int amount) {

    // Add all quest types to optionCounterForQuestType
    Arrays.stream(QuestType.values())
        .forEach(
            questType -> {
              Map<GenerationOption, Integer> materialMap = new HashMap<>();
              GenerationConfig questTypeConfig =
                  GenerationFileService.getInstance().getConfigForQuestType(questType);
              if (questTypeConfig.getOptions() != null && !questTypeConfig.getOptions().isEmpty()) {
                questTypeConfig
                    .getOptions()
                    .forEach(generationOption -> materialMap.put(generationOption, 0));
              }
              optionCounterForQuestType.put(questType, materialMap);
            });

    List<Quest> generatedQuests = new ArrayList<>();

    // Outputting x example quests in console (balancing purpose)
    for (int i = 0; i < amount; i++) {
      try {
        Quest q = QuestGenerator.getInstance().generate(player);
        generatedQuests.add(q);
        BasicQuestsPlugin.log(q.debugString());

      } catch (QuestGenerationException e) {
        BasicQuestsPlugin.log(Level.SEVERE, e.getMessage());
      }
    }

    checkQuests(generatedQuests);

    validateAndDisplayMaterialCountsForQuestType();
  }

  public void checkQuests(List<Quest> quests) {

    for (Quest quest : quests) {
      // has reward?
      assert quest.getReward() != null;
      List<ItemStack> itemReward = quest.getReward().getItems();
      int xpReward = quest.getReward().getXp();
      BigDecimal moneyReward = quest.getReward().getMoney();
      boolean hasReward =
          (itemReward != null && !itemReward.isEmpty())
              || xpReward > 0
              || moneyReward.compareTo(BigDecimal.ZERO) > 0;
      assert hasReward;

      // valid amount
      GenerationConfig questTypeConfig =
          GenerationFileService.getInstance().getConfigForQuestType(quest.getQuestType());
      if (questTypeConfig.getOptions() != null && !questTypeConfig.getOptions().isEmpty()) {

        // find GenerationOption
        String materialName = getMaterialNameForQuest(quest);
        increaseMaterialForQuestType(materialName, quest.getQuestType());
        Optional<GenerationOption> generationOption =
            questTypeConfig.getOptions().stream()
                .filter(option -> option.getName().equals(materialName))
                .findFirst();
        assert generationOption.isPresent();

        int minAmount =
            (generationOption.get().getMin() == 0)
                ? questTypeConfig.getDefault_min()
                : generationOption.get().getMin();
        int maxAmount =
            (generationOption.get().getMax() == 0)
                ? questTypeConfig.getDefault_max()
                : generationOption.get().getMax();
        int step =
            (generationOption.get().getStep() == 0)
                ? questTypeConfig.getDefault_step()
                : generationOption.get().getStep();

        maxAmount = Math.max(maxAmount, 1);
        step = Math.max(step, 1);

        // Check if amount is valid
        assert quest.getGoal() >= minAmount;
        assert quest.getGoal() <= maxAmount;
        assert 0 == quest.getGoal() % step;

        // Check if all materials have been generated
      }
    }
  }

  public String getMaterialNameForQuest(Quest quest) {
    String optionName = quest.getOptionKey();
    if (optionName.equals("VILLAGER")) {
      optionName = "NONE";
    }
    return optionName;
  }

  public void increaseMaterialForQuestType(String materialString, QuestType questType) {

    Map<GenerationOption, Integer> materialCounterMap = optionCounterForQuestType.get(questType);

    Optional<GenerationOption> materialOption =
        materialCounterMap.keySet().stream()
            .filter(generationOption -> generationOption.getName().equals(materialString))
            .findFirst();
    assert materialOption.isPresent();

    materialCounterMap.put(materialOption.get(), materialCounterMap.get(materialOption.get()) + 1);
  }

  private void validateAndDisplayMaterialCountsForQuestType() {
    StringBuilder sb = new StringBuilder("\n\nMaterial counts for Quest Type:");

    for (Map.Entry<QuestType, Map<GenerationOption, Integer>> materialMapEntry :
        optionCounterForQuestType.entrySet()) {
      sb.append("\n\nQuest Type: ").append(materialMapEntry.getKey().name());
      Optional<Integer> questTypeCount =
          materialMapEntry.getValue().values().stream().reduce(Integer::sum);

      double totalOptionWeight = 0;
      for (Map.Entry<GenerationOption, Integer> materialEntry :
          materialMapEntry.getValue().entrySet()) {
        totalOptionWeight += materialEntry.getKey().getWeight();
      }

      for (Map.Entry<GenerationOption, Integer> materialEntry :
          materialMapEntry.getValue().entrySet()) {
        double actualWeight = (double) materialEntry.getValue() / questTypeCount.get();

        String materialCountString =
            String.format(
                "%-20s count: %-5s        %5.2f %%    (%-5.2f%% expected)",
                materialEntry.getKey().getName(),
                materialEntry.getValue(),
                actualWeight * 100,
                materialEntry.getKey().getWeight() / totalOptionWeight * 100);
        sb.append("\n- ").append(materialCountString);

        assert Math.abs(materialEntry.getKey().getWeight() - actualWeight) < 0.1;
      }
    }

    BasicQuestsPlugin.log(sb.toString());
  }
}
