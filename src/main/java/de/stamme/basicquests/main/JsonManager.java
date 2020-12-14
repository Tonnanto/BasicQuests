package de.stamme.basicquests.main;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import de.stamme.basicquests.quest_generation.DecisionObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class JsonManager {
	
	public static Gson gson = new Gson();

	@SuppressWarnings("unchecked")
	public static Map<String, Object> read(String path) {
				
        try {
        	StringBuilder sb = new StringBuilder();
        	InputStream stream = JsonManager.class.getResourceAsStream(path);
        	
        	BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        	 
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }

			return gson.fromJson(sb.toString(), Map.class);
        	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	// looks for the key "decisionObjects" in the given map and tries to map it's value to a list of DecisionObjects
	public static ArrayList<DecisionObject> getDecisionObjects(Map<String, Object> jsonMap) {
		ArrayList<DecisionObject> list = new ArrayList<>();

		Object do_value = jsonMap.get("decisionObjects");
		
		if (do_value instanceof ArrayList<?>) {
			ArrayList<?> do_list = (ArrayList<?>) do_value;
			
			for (Object object: do_list) {
								
				if (object instanceof LinkedTreeMap) {
					LinkedTreeMap<?, ?> do_map = (LinkedTreeMap<?, ?>) object;
					String do_string = do_map.toString();
					DecisionObject obj = gson.fromJson(do_string, DecisionObject.class);
					list.add(obj);
				}
			}
		}
		
		return list;
	}
	
	public static ArrayList<DecisionObject> getDecisionObjects(String path) {
		Map<String, Object> jsonMap = read(path);
		assert jsonMap != null;
		return getDecisionObjects(jsonMap);
	}
}
