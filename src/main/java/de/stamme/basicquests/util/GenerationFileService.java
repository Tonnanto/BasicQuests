package de.stamme.basicquests.util;

import com.google.gson.Gson;
import de.stamme.basicquests.BasicQuestsPlugin;
import de.stamme.basicquests.model.generation.GenerationConfig;
import de.stamme.basicquests.model.generation.GenerationOption;
import de.stamme.basicquests.model.rewards.ItemRewardType;
import de.stamme.basicquests.model.quests.QuestType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class GenerationFileService {
    private static final String questGenerationBasePath = BasicQuestsPlugin.getPlugin().getDataFolder() + File.separator + "quest_generation" + File.separator;
    private static final String itemRewardGenerationBasePath = questGenerationBasePath  + File.separator + "item_reward_generation" + File.separator;
    private static GenerationFileService instance;

    // Singleton
    public static GenerationFileService getInstance() {
        if (instance == null) {
            instance = new GenerationFileService();
        }
        return instance;
    }

    private YamlConfiguration questTypesYaml;
    private final Map<QuestType, YamlConfiguration> yamlForQuestType;
    private final Map<ItemRewardType, YamlConfiguration> yamlForItemRewardType;

    private GenerationConfig questTypeConfig;
    private final Map<QuestType, GenerationConfig> configForQuestType;
    private final Map<ItemRewardType, GenerationConfig> configForItemRewardType;


    private GenerationFileService() {
        yamlForQuestType = new HashMap<>();
        configForQuestType = new HashMap<>();
        yamlForItemRewardType = new HashMap<>();
        configForItemRewardType = new HashMap<>();

        loadDefaultGenerationFiles();
    }

    /**
     * Saves default generations files to plugin folder (skips already existing files)
     * Loads configurations into memory
     */
    private void loadDefaultGenerationFiles() {
        int savedFiles = 0;

        // save generation README file
        File readmeFile = new File(questGenerationBasePath + "README.md");
        if (!readmeFile.exists()) {
            savedFiles++;
            BasicQuestsPlugin.getPlugin().saveResource("quest_generation/README.md", true);
        }

        // save quest types file
        File questTypesConfigFile = new File(questGenerationBasePath + "quest_types.yml");
        if (!questTypesConfigFile.exists()) {
            savedFiles++;
            BasicQuestsPlugin.getPlugin().saveResource("quest_generation/quest_types.yml", false);
        }
        try {
            questTypesYaml = YamlConfiguration.loadConfiguration(questTypesConfigFile);
        } catch (Exception e) {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not parse file " + questTypesConfigFile.getPath() );
        }

        // save file for every quest type
        for (QuestType questType: QuestType.values()) {
            File configFile = new File(questGenerationBasePath + questType.name().toLowerCase() + ".yml");
            if (!configFile.exists()) {
                savedFiles++;
                BasicQuestsPlugin.getPlugin().saveResource("quest_generation/" + questType.name().toLowerCase() + ".yml", false);
            }
            try {
                yamlForQuestType.put(questType, YamlConfiguration.loadConfiguration(configFile));
            } catch (Exception e) {
                BasicQuestsPlugin.log(Level.SEVERE, "Could not parse file " + configFile.getPath() );
            }
        }

        // save file for every reward type
        for (ItemRewardType itemRewardType: ItemRewardType.values()) {
            File configFile = new File(itemRewardGenerationBasePath + itemRewardType.name().toLowerCase() + ".yml");
            if (!configFile.exists()) {
                savedFiles++;
                BasicQuestsPlugin.getPlugin().saveResource("quest_generation/item_reward_generation/" + itemRewardType.name().toLowerCase() + ".yml", false);
            }
            try {
                yamlForItemRewardType.put(itemRewardType, YamlConfiguration.loadConfiguration(configFile));
            } catch (Exception e) {
                BasicQuestsPlugin.log(Level.SEVERE, "Could not parse file " + configFile.getPath() );
            }
        }

        // Log
        if (savedFiles != 0) {
            BasicQuestsPlugin.log("Created " + savedFiles + " quest generation config files at " + questGenerationBasePath);
        }
    }

    public GenerationConfig getQuestTypeGenerationConfig() {
        if (questTypeConfig != null) return questTypeConfig.clone();
        questTypeConfig = getGenerationConfigFromYamlSection(getQuestTypesYaml());
        assert questTypeConfig != null;
        return questTypeConfig.clone();
    }

    public GenerationConfig getConfigForQuestType(QuestType questType) {
        if (configForQuestType.get(questType) != null)
            return configForQuestType.get(questType).clone();
        ConfigurationSection configurationSection = getYamlForQuestType(questType).getRoot();
        GenerationConfig generationConfig = getGenerationConfigFromYamlSection(configurationSection);
        configForQuestType.put(questType, generationConfig);
        assert generationConfig != null;
        return generationConfig.clone();
    }

    public GenerationConfig getConfigForItemRewardType(ItemRewardType itemRewardType) {
        if (configForItemRewardType.get(itemRewardType) != null)
            return configForItemRewardType.get(itemRewardType).clone();
        ConfigurationSection configurationSection = getYamlForItemRewardType(itemRewardType).getRoot();
        GenerationConfig generationConfig = getGenerationConfigFromYamlSection(configurationSection);
        configForItemRewardType.put(itemRewardType, generationConfig);
        assert generationConfig != null;
        return generationConfig.clone();
    }

    @Nullable
    private GenerationConfig getGenerationConfigFromYamlSection(ConfigurationSection yamlSection) {
        if (yamlSection == null)
            return null;

        List<?> optionsList = yamlSection.getList("options");
        List<GenerationOption> options = getOptionsFromOptionList(optionsList);

        int defaultMin = yamlSection.getInt("default_min");
        int defaultMax = yamlSection.getInt("default_max");
        int defaultStep = yamlSection.getInt("default_step");
        double valuePerUnit = yamlSection.getDouble("value_per_unit");

        return new GenerationConfig(defaultMin, defaultMax, defaultStep, valuePerUnit, options);
    }

    private List<GenerationOption> getOptionsFromOptionList(List<?> optionList) {
        if (optionList == null)
            return null;

        Gson gson = new Gson();
        List<GenerationOption> options = new ArrayList<>();
        double totalOptionWeight = 0;

        for (Object generationOption : optionList) {
            if (!(generationOption instanceof LinkedHashMap)) {
                BasicQuestsPlugin.log(Level.SEVERE, "Could not parse from generation file: " + generationOption);
                continue;
            }

            LinkedHashMap optionMap = (LinkedHashMap) generationOption;
            String name = (String) optionMap.keySet().iterator().next();

            if (!(optionMap.get(name) instanceof LinkedHashMap)) {
                BasicQuestsPlugin.log(Level.SEVERE, "Could not parse from generation file: " + generationOption);
                continue;
            }

            optionMap.putAll((LinkedHashMap) optionMap.get(name));
            optionMap.remove(name);
            optionMap.put("name", name.toUpperCase().replace("-", "_"));

            if (optionMap.containsKey("options") && optionMap.get("options") instanceof List<?>) {
                optionMap.put("options", getOptionsFromOptionList((List<?>) optionMap.get("options")));
            }

            String json = gson.toJson(optionMap, LinkedHashMap.class);
            GenerationOption option = gson.fromJson(json, GenerationOption.class);
            totalOptionWeight += option.getWeight();

            options.add(option);
        }

        double finalTotalOptionWeight = totalOptionWeight;
        options.forEach(generationOption -> {
            if (finalTotalOptionWeight == 0) return;
            generationOption.setWeight(generationOption.getWeight() / finalTotalOptionWeight);
        });

        return options;
    }

    private YamlConfiguration getQuestTypesYaml() {
        return questTypesYaml;
    }

    private YamlConfiguration getYamlForQuestType(QuestType questType) {
        return yamlForQuestType.get(questType);
    }

    public YamlConfiguration getYamlForItemRewardType(ItemRewardType itemRewardType) {
        return yamlForItemRewardType.get(itemRewardType);
    }

    public static void reload() {
        instance = new GenerationFileService();
    }
}
