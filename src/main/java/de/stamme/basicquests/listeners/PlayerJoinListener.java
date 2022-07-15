package de.stamme.basicquests.listeners;

import de.stamme.basicquests.util.GenerationFileService;
import de.stamme.basicquests.Main;
import de.stamme.basicquests.model.PlayerData;
import de.stamme.basicquests.model.QuestPlayer;
import de.stamme.basicquests.model.generation.GenerationConfig;
import de.stamme.basicquests.model.generation.GenerationOption;
import de.stamme.basicquests.model.generation.QuestGenerationException;
import de.stamme.basicquests.questgeneration.QuestGenerator;
import de.stamme.basicquests.model.quests.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
		Player player = event.getPlayer();

		// load player data from file - if not successful generate new QuestPlayer
		if (!PlayerData.loadPlayerData(player)) {
			QuestPlayer joinedPlayer = new QuestPlayer(player);
			Main.getPlugin().getQuestPlayers().put(player.getUniqueId(), joinedPlayer);
		}







		if (Main.getPlugin().getQuestPlayers().containsKey(player.getUniqueId())) {

			QuestPlayer questPlayer = Main.getPlugin().getQuestPlayers().get(player.getUniqueId());


			// Add all quest types to optionCounterForQuestType
			Arrays.stream(QuestType.values()).forEach(questType -> {
				Map<GenerationOption, Integer> materialMap = new HashMap<>();
				GenerationConfig questTypeConfig =  GenerationFileService.getInstance().getConfigForQuestType(questType);
				if (questTypeConfig.getOptions() != null && !questTypeConfig.getOptions().isEmpty()) {
					questTypeConfig.getOptions().forEach(generationOption -> materialMap.put(generationOption, 0));
				}
				optionCounterForQuestType.put(questType, materialMap);
			});

			List<Quest> generatedQuests = new ArrayList<>();

			// Outputting 100 example quests in console (balancing purpose)
			for (int i = 0; i < 5000; i++) {
				try {
					Quest q = QuestGenerator.getInstance().generate(questPlayer);
					generatedQuests.add(q);
					Main.log(q.getInfo(true));

				} catch (QuestGenerationException e) {
					Main.log(e.message);
					e.printStackTrace();
				}
			}

			checkQuests(generatedQuests);

			validateAndDisplayMaterialCountsForQuestType();
		}
	}


	private final Map<QuestType, Map<GenerationOption, Integer>> optionCounterForQuestType = new HashMap<>();


	public void checkQuests(List<Quest> quests) {

		for (Quest quest: quests) {
			// has reward?
			assert quest.getReward() != null;
			List<ItemStack> itemReward = quest.getReward().getItems();
			int xpReward = quest.getReward().getXp();
			BigDecimal moneyReward = quest.getReward().getMoney();
			boolean hasReward = (itemReward != null && !itemReward.isEmpty()) || xpReward > 0 || moneyReward.compareTo(BigDecimal.ZERO) > 0;
			assert hasReward;

			// valid amount
			GenerationConfig questTypeConfig =  GenerationFileService.getInstance().getConfigForQuestType(quest.getQuestType());
			if (questTypeConfig.getOptions() != null && !questTypeConfig.getOptions().isEmpty()) {

				// find GenerationOption
				String materialName = getMaterialNameForQuest(quest);
				increaseMaterialForQuestType(materialName, quest.getQuestType());
				Optional<GenerationOption> generationOption = questTypeConfig.getOptions().stream().filter(option -> option.getName().equals(materialName)).findFirst();
				assert generationOption.isPresent();


				int minAmount = (generationOption.get().getMin() == 0) ? questTypeConfig.getDefault_min() : generationOption.get().getMin();
				int maxAmount = (generationOption.get().getMax() == 0) ? questTypeConfig.getDefault_max() : generationOption.get().getMax();
				int step = (generationOption.get().getStep() == 0) ? questTypeConfig.getDefault_step() : generationOption.get().getStep();

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

				assert Math.abs(materialEntry.getKey().getWeight() - actualWeight) < 0.1;
			}
		}

		Main.log(sb.toString());
	}
}
