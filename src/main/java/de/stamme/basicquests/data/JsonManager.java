package de.stamme.basicquests.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.quest_generation.DecisionObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonManager {
	
	public static Gson gson = new Gson();

	public static Map<String, Object> read(String path) {
				
        try {
        	StringBuilder sb = new StringBuilder();
        	InputStream stream = Main.class.getResourceAsStream(path);

			assert stream != null;
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        	 
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }

			GsonBuilder gson = new GsonBuilder();
			Type collectionType = new TypeToken<Map<String, Object>>(){}.getType();

			return gson.create().fromJson(sb.toString(), collectionType);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * looks for the key "decisionObjects" in the given map and tries to map it's value to a list of DecisionObjects
	 * @param jsonMap the map to look in
	 * @return Lis of DecisionObjects
	 */
	public static List<DecisionObject> getDecisionObjects(Map<?, ?> jsonMap) {
		List<DecisionObject> list = new ArrayList<>();

		Object doValue = jsonMap.get("decisionObjects");
		
		if (doValue instanceof List<?>) {
			List<?> doList = (List<?>) doValue;
			
			for (Object object: doList) {
								
				if (object instanceof LinkedTreeMap) {
					LinkedTreeMap<?, ?> doMap = (LinkedTreeMap<?, ?>) object;
					String do_string = doMap.toString();
					DecisionObject obj = gson.fromJson(do_string, DecisionObject.class);
					list.add(obj);
				}
			}
		}
		
		return list;
	}
	
	public static List<DecisionObject> getDecisionObjects(String path) {
		Map<String, Object> jsonMap = read(path);
		assert jsonMap != null;
		return getDecisionObjects(jsonMap);
	}
}
