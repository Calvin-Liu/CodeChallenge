import java.util.*;
import java.io.*;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ParseJSON {
	private ArrayList<String> created_at;
	private Set<String> hashtags;
	private Map<String, String> dictionary;
	private JSONParser parser;
	private boolean found = false;

	@SuppressWarnings("unchecked")

	public ParseJSON() {
		created_at = new ArrayList<String>();
		hashtags = new HashSet<String>();
		parser = new JSONParser();
		dictionary = new HashMap<String, String>();
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
			while(iterator.hasNext()) {
				String hash_text = (String)iterator.next().get("text");
				if(hashtags.contains(hash_text)) { //If it is already in the graph
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double averageEdges(HashMap<String, String> nodes, HashSet<String> uniqueHashes) {
		return  nodes.size() / uniqueHashes.size();
	}
}