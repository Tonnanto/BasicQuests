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
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * 1. Checks if all configuration files exist and are up to date.
     * 2. Saves default config files that are missing or outdated.
     * 3. Loads configurations into memory.
     */
    private void loadDefaultGenerationFiles() {
        int savedFiles = 0;

        // save generation README file
        File readmeFile = new File(questGenerationBasePath + "README.md");
        String readmeContent = readFile(readmeFile);
        if (readmeContent == null || isOutdated(readmeContent)) {
            savedFiles++;
            BasicQuestsPlugin.getPlugin().saveResource("quest_generation/README.md", true);
        }

        // save quest types file
        File questTypesConfigFile = new File(questGenerationBasePath + "quest_types.yml");
        String questTypesConfigContent = readFile(questTypesConfigFile);
        if (questTypesConfigContent == null || isOutdated(questTypesConfigContent)) {
            savedFiles++;
            migrateGenerationFile(questGenerationBasePath + "quest_types.yml", "quest_generation/quest_types.yml");
        }
        try {
            questTypesYaml = YamlConfiguration.loadConfiguration(questTypesConfigFile);
        } catch (Exception e) {
            BasicQuestsPlugin.log(Level.SEVERE, "Could not parse file " + questTypesConfigFile.getPath() );
        }

        // save file for every quest type
        for (QuestType questType: QuestType.values()) {
            File configFile = new File(questGenerationBasePath + questType.name().toLowerCase() + ".yml");
            String configFileContent = readFile(configFile);
            if (configFileContent == null || isOutdated(configFileContent)) {
                savedFiles++;
                migrateGenerationFile(questGenerationBasePath + questType.name().toLowerCase() + ".yml", "quest_generation/" + questType.name().toLowerCase() + ".yml");
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
            String configFileContent = readFile(configFile);
            if (configFileContent == null || isOutdated(configFileContent)) {
                savedFiles++;
                migrateGenerationFile(itemRewardGenerationBasePath + itemRewardType.name().toLowerCase() + ".yml", "quest_generation/item_reward_generation/" + itemRewardType.name().toLowerCase() + ".yml");
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

    /**
     * Reads the contents of a file as a single string.
     *
     * @param file the file to read
     * @return the content of the file
     */
    @Nullable
    private String readFile(File file) {
        if (!file.exists()) return null;
        if (!file.canRead()) return null;
        try {
            return Files.readAllLines(file.toPath()).stream()
                .map(String::valueOf)
                .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks whether a config file is outdated.
     * Each config file contains a version string with the version the file has been generated for.
     *
     * @param fileContent the content of the file to check
     * @return whether the file is outdated
     */
    private boolean isOutdated(String fileContent) {
        Pattern versionPattern = Pattern.compile("version [0-9.]+\\b");

        // Looking for version String in file
        Matcher m = versionPattern.matcher(fileContent);
        if (m.find()) {
            String s = m.group();
            return !s.equalsIgnoreCase(getCurrentVersionString());
        }
        return true;
    }

    /**
     * Replaces old generation file with updated file of newer version.
     * Replaces all new values that were also present in the old version with their past values.
     *
     * @param filePath path of the old file tht should be replaced
     * @param newResourcePath resource path of the internal template file from the new version
     */
    private void migrateGenerationFile(String filePath, String newResourcePath) {
        try {
            // Store old config values
            File oldFile = new File(filePath);
            YamlConfiguration oldConfiguration = YamlConfiguration.loadConfiguration(oldFile);

            // Replace old file with new file
            BasicQuestsPlugin.getPlugin().saveResource(newResourcePath, true);

            // Return if there is no old file to migrate from
            if (!oldFile.exists()) {
                return;
            }

            // Load new configuration
            File newFile = new File(filePath);
            YamlConfiguration newConfiguration = YamlConfiguration.loadConfiguration(newFile);

            List<?> newOptionsList = newConfiguration.getList("options");
            List<GenerationOption> newConfigOptions = getOptionsFromOptionList(newOptionsList);

            // Update option values with old option values if available
            List<?> oldOptionsList = oldConfiguration.getList("options");
            List<GenerationOption> oldConfigOptions = getOptionsFromOptionList(oldOptionsList);
            for (GenerationOption option: newConfigOptions) {
                Optional<GenerationOption> matchingOldOption = oldConfigOptions.stream().filter(o -> o.getName().equalsIgnoreCase(option.getName())).findFirst();
                matchingOldOption.ifPresent(option::updateWith);
            }

            if (!newConfigOptions.isEmpty()) {
                newConfiguration.set("options", newConfigOptions.stream().map(GenerationOption::toMap).collect(Collectors.toList()));
            }

            // Update remaining values
            for (String key: newConfiguration.getKeys(false)) {
                if (key.equalsIgnoreCase("options")) continue;
                if (oldConfiguration.getKeys(false).contains(key)) {
                    newConfiguration.set(key, oldConfiguration.get(key));
                }
            }

            // Save new configuration to file
            newConfiguration.save(newFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the current version string in config files
     */
    private static String getCurrentVersionString() {
        String version = BasicQuestsPlugin.getPlugin().getDescription().getVersion();
        return "version " + version;
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
            return new ArrayList<>();

        Gson gson = new Gson();
        List<GenerationOption> options = new ArrayList<>();

        for (Object generationOption: optionList) {
            if (!(generationOption instanceof LinkedHashMap<?, ?>)) {
                BasicQuestsPlugin.log(Level.SEVERE, "Could not parse from generation file: " + generationOption);
                continue;
            }

            LinkedHashMap<String, Object> optionMap = (LinkedHashMap<String, Object>) generationOption;
            String name = optionMap.keySet().iterator().next();

            if (!(optionMap.get(name) instanceof LinkedHashMap<?, ?>)) {
                BasicQuestsPlugin.log(Level.SEVERE, "Could not parse from generation file: " + generationOption);
                continue;
            }

            optionMap.putAll((LinkedHashMap<String, Object>) optionMap.get(name));
            optionMap.remove(name);
            optionMap.put("name", name.toUpperCase().replace("-", "_"));

            if (optionMap.containsKey("options") && optionMap.get("options") instanceof List<?>) {
                optionMap.put("options", getOptionsFromOptionList((List<?>) optionMap.get("options")));
            }

            String json = gson.toJson(optionMap, LinkedHashMap.class);
            GenerationOption option = gson.fromJson(json, GenerationOption.class);
            options.add(option);
        }

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
