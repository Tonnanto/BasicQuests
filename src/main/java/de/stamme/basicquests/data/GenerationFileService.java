package de.stamme.basicquests.data;

import com.google.gson.Gson;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.quest_generation.DecisionObject;
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

    private List<DecisionObject> questTypeDos;

    private List<DecisionObject> chopWoodDos;

    private GenerationFileService() {
        File file = new File(Main.getPlugin().getDataFolder() + File.separator + generationFilePath);
        generationConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public List<DecisionObject> getQuestTypeDecisionObjects() {
        if (questTypeDos != null) return questTypeDos;
        questTypeDos = getDecisionObjectsFromYamlList(generationConfiguration.getList("quest-types"));
        return questTypeDos;
    }

    public List<DecisionObject> getChopWoodDecisionObjects() {
        if (chopWoodDos != null) return chopWoodDos;
        chopWoodDos = getDecisionObjectsFromYamlList(generationConfiguration.getList("chop-wood.materials"));
        return chopWoodDos;
    }

    @Nullable
    private List<DecisionObject> getDecisionObjectsFromYamlList(List<?> yamlList) {
        if (yamlList == null) return null;
        Gson gson = new Gson();

        List<DecisionObject> decisionObjects = new ArrayList<>();

        for (Object questType : yamlList) {

            if (!(questType instanceof LinkedHashMap)) continue;

            LinkedHashMap questTypeMap = (LinkedHashMap) questType;
            String name = (String) questTypeMap.keySet().iterator().next();

            if (!(questTypeMap.get(name) instanceof LinkedHashMap)) continue;

            questTypeMap.putAll((LinkedHashMap) questTypeMap.get(name));
            questTypeMap.put(name, null);
            questTypeMap.put("name", name.toUpperCase().replace("-", "_"));

            String json = gson.toJson(questTypeMap, LinkedHashMap.class);
            DecisionObject obj = gson.fromJson(json, DecisionObject.class);

            decisionObjects.add(obj);
        }

        System.out.println(decisionObjects);
        return decisionObjects;
    }
}
