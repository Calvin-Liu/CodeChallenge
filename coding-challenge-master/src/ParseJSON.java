import java.util.*;
import java.io.*;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ParseJSON {
	private ArrayList<String> created_at;
	private Set<String> hashtags;
    private Set<String> aux_hashtags;
	private Map<String, List<String>> dictionary;
	private JSONParser parser;
	private boolean found = false;

	@SuppressWarnings("unchecked")

	public ParseJSON() {
		created_at = new ArrayList<String>();
		hashtags = new HashSet<String>();
        aux_hashtags = new HashSet<String>();
		parser = new JSONParser();
		dictionary = new HashMap<String, List<String>>();
	}

	public void read(FileReader file) throws IOException {
		try {
			Object obj = parser.parse(file);

			JSONObject jsonObject = (JSONObject) obj;

			String single_created_at = (String) jsonObject.get("created_at");
			JSONObject single_entities = (JSONObject) jsonObject.get("entities");
			JSONArray single_hash_tag = (JSONArray) single_entities.get("hashtags");

			created_at.add(single_created_at);
			Iterator<JSONObject> iterator = single_hash_tag.iterator();
            //Check all the hashtags in the hashtag field
			while(iterator.hasNext()) {
                String hash_text = (String) iterator.next().get("text");
                aux_hashtags.add(hash_text);
            }

            for(String s : aux_hashtags) {
                List<String> listOfTags = new ArrayList<String>();
                for(String t : aux_hashtags) {
                    if(s != t) {
                        listOfTags.add(t);
                    }
                }
                dictionary.put(s, listOfTags);
            }

            aux_hashtags = null;

            averageEdges(dictionary, hashtags);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double averageEdges(HashMap<String, List<String>> nodes, HashSet<String> uniqueHashes) {
        int total_edges = 0;
        for(List<String> value : nodes.values()) {
           total_edges += value.size();
        }
		return  total_edges / uniqueHashes.size();
	}
}