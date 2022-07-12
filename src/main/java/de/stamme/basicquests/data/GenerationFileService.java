package de.stamme.basicquests.data;

import com.google.gson.Gson;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.questgeneration.GenerationOption;
import de.stamme.basicquests.questgeneration.GenerationConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GenerationFileService {
    static public String generationFilePath = "quest_generation.yml";

    private static GenerationFileService instance;

    // Singleton
    public static GenerationFileService getInstance() {
        if (instance == null) {
            instance = new GenerationFileService();
        }
        return instance;
    }

    private final YamlConfiguration generationConfiguration;

    private GenerationConfig questTypeDos;

    private GenerationConfig chopWoodConfig;
    private GenerationConfig mineBlockDos;

    private GenerationFileService() {
        File file = new File(Main.getPlugin().getDataFolder() + File.separator + generationFilePath);
        generationConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public GenerationConfig getQuestTypeGenerationConfig() {
        if (questTypeDos != null) return questTypeDos;
        questTypeDos = getGenerationConfigFromYamlSection(getConfiguration().getConfigurationSection("quest-types"));
        return questTypeDos;
    }

    public GenerationConfig getChopWoodGenerationConfig() {
        if (chopWoodConfig != null) return chopWoodConfig;
        chopWoodConfig = getGenerationConfigFromYamlSection(getConfiguration().getConfigurationSection("chop-wood"));
        return chopWoodConfig;
    }

    public GenerationConfig getMineBlockGenerationConfig() {
        if (mineBlockDos != null) return mineBlockDos;
        mineBlockDos = getGenerationConfigFromYamlSection(getConfiguration().getConfigurationSection("mine-block"));
        return mineBlockDos;
    }

    @Nullable
    private GenerationConfig getGenerationConfigFromYamlSection(ConfigurationSection yamlSection) {
        if (yamlSection == null) return null;
        List<?> optionsList = yamlSection.getList("options");
        if (optionsList == null) return null;

        Gson gson = new Gson();


        List<GenerationOption> options = new ArrayList<>();

        for (Object generationOption : optionsList) {

            if (!(generationOption instanceof LinkedHashMap)) continue;

            LinkedHashMap questTypeMap = (LinkedHashMap) generationOption;
            String name = (String) questTypeMap.keySet().iterator().next();

            if (!(questTypeMap.get(name) instanceof LinkedHashMap)) continue;

            questTypeMap.putAll((LinkedHashMap) questTypeMap.get(name));
            questTypeMap.put(name, null);
            questTypeMap.put("name", name.toUpperCase().replace("-", "_"));

            String json = gson.toJson(questTypeMap, LinkedHashMap.class);
            GenerationOption obj = gson.fromJson(json, GenerationOption.class);

            options.add(obj);
        }

        System.out.println(options);

        int defaultMin = yamlSection.getInt("default_min");
        int defaultMax = yamlSection.getInt("default_max");
        int defaultStep = yamlSection.getInt("default_step");

        return new GenerationConfig(defaultMin, defaultMax, defaultStep, options);
    }

    public YamlConfiguration getConfiguration() {
        return generationConfiguration;
    }
}
