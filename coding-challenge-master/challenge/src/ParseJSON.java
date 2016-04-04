import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ParseJSON {
	private LinkedList<Date> created_at;
	private Map<String, Integer> hashtags;
    private Set<String> aux_hashtags;
	private Map<String, List<String>> dictionary;
    private Map<String, String> duplicateEdgesDictionary;
	private JSONParser parser;
    private SimpleDateFormat earliestDTO;
    private Date earliestD;
    private LinkedList<Set<String>> earliestHTs;
    private Set<String> earliestHT;
    private Set<String> aux_HT;

	@SuppressWarnings("unchecked")

	public ParseJSON() {
		created_at = new LinkedList<Date>();
		hashtags = new HashMap<String, Integer>();
        aux_hashtags = new HashSet<String>();
		parser = new JSONParser();
		dictionary = new HashMap<String, List<String>>();
        duplicateEdgesDictionary = new HashMap<String, String>();
        earliestDTO = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        earliestD = null;
        earliestHTs = new LinkedList<Set<String>>();
        earliestHT = new HashSet<String>();
        aux_HT = new HashSet<String>();
	}

	public void read(FileReader file, String outputLocation) throws IOException {
        Scanner scan = new Scanner(file);
        while(scan.hasNextLine()) {
            try {
                Object obj = parser.parse(scan.nextLine());

                JSONObject jsonObject = (JSONObject) obj;

                String single_created_at = (String) jsonObject.get("created_at");
                String trimmed_string = single_created_at.substring(0,19) + single_created_at.substring(25,30);

                JSONObject single_entities = (JSONObject) jsonObject.get("entities");
                JSONArray single_hash_tag = (JSONArray) single_entities.get("hashtags");
                Iterator<JSONObject> iterator = single_hash_tag.iterator();
                //Check all the hashtags in the hashtag field
                while (iterator.hasNext()) {
                    String hash_text = (String) iterator.next().get("text");
                    aux_hashtags.add(hash_text);
                }

                if(earliestD == null) {
                    //Save the earliest tweet date and hashes
                    earliestD = earliestDTO.parse(trimmed_string);
                    created_at.addLast(earliestD);
                    //Add individually, clearing aux later will clear earliestHT
                    //Same object if you just use earliestHT = aux_hashtags
                    for(String p : aux_hashtags) {
                        earliestHT.add(p);
                    }
                    //Add to list of tweets
                    earliestHTs.addLast(earliestHT);
                } else {
                    //Check current processing tweet
                    DateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
                    Date d = df.parse(trimmed_string);
                    long diffInSeconds = (d.getTime() - earliestD.getTime())/1000;
                    //The incoming tweet is out of order and > 60 seconds
                    if(diffInSeconds < 0) {
                        aux_hashtags.clear();
                        continue;
                    }
                    if(diffInSeconds > 60) {
                        earliestHT = earliestHTs.removeFirst();

                        //Remove necessary nodes
                        for(String e : earliestHT) {
                            if(dictionary.get(e).isEmpty()) {
                                hashtags.remove(e);
                            } else {
                                hashtags.put(e, hashtags.get(e)-1);
                            }
                        }

                        removeAnyEmptyNodes(hashtags);

                        //Remove the evicted hash edges from dictionary
                        for(String temp : earliestHT) {
                            for(String temp1: earliestHT) {
                                if(temp != temp1) {
                                    if(duplicateEdgesDictionary.containsKey(temp) && duplicateEdgesDictionary.containsValue(temp1)) {
                                        duplicateEdgesDictionary.remove(temp);
                                    } else {
                                        dictionary.get(temp).remove(temp1);
                                    }
                                }
                            }
                        }

                        //Set new earliest tweet
                        created_at.removeFirst();
                        earliestD = created_at.getFirst();
                        created_at.addLast(d);
                        earliestHT = earliestHTs.getFirst();
                        for(String p : aux_hashtags) {
                            aux_HT.add(p);
                        }
                        earliestHTs.addLast(aux_HT);
                        aux_HT = new HashSet<String>();
                    } else {
                        created_at.addLast(d);
                        for(String p : aux_hashtags) {
                            aux_HT.add(p);
                        }
                        earliestHTs.addLast(aux_HT);
                        aux_HT = new HashSet<String>();
                    }
                }


                //Check if one of the hashes is in the graph
                if(hashTagIsInGraph(hashtags, aux_hashtags)) {
                    for(String s : aux_hashtags) {
                        List<String> listOfTags = new ArrayList<String>();
                        for(String t : aux_hashtags) {
                            if (s != t) {
                                listOfTags.add(t);
                            }
                        }
                        if(!hashtags.containsKey(s)) {
                            hashtags.put(s, 1);
                            dictionary.put(s, new ArrayList<String>());
                        } else {
                            hashtags.put(s, hashtags.get(s)+1);
                        }
                        //Add only the hashes that are not already in the dictionary under that key
                        for(String k : listOfTags) {
                            if(!dictionary.get(s).contains(k)) {
                                dictionary.get(s).add(k);
                            } else {
                                duplicateEdgesDictionary.put(s, k);
                            }
                        }

                    }
                } else {
                    for (String s : aux_hashtags) {
                        List<String> listOfTags = new ArrayList<String>();
                        for (String t : aux_hashtags) {
                            if (s != t) {
                                listOfTags.add(t);
                            }
                        }
                        if(hashtags.containsKey(s)) {
                            hashtags.put(s, hashtags.get(s) + 1);
                        } else {
                            hashtags.put(s, 1);
                        }
                        dictionary.put(s, listOfTags);
                    }
                }

                aux_hashtags.clear();

            } catch (Exception e) {
                e.printStackTrace();
            }

            String avg_edge = truncate(averageEdges(dictionary, hashtags));
            writeToOutput(avg_edge, outputLocation);
        }
        scan.close();
	}

	public double averageEdges(Map<String, List<String>> nodes, Map<String, Integer> uniqueHashes) {
        double total_edges = 0.00;
        //Sum all the edge counts and divide by the # of nodes
        for(List<String> value : nodes.values()) {
           total_edges += value.size();
        }
		return  total_edges / (double)uniqueHashes.size();
	}

    public boolean hashTagIsInGraph(Map<String, Integer> a, Set<String> b) {
        //Iterate through b
        for(String temp : b) {
            //If hashtags has an item in the graph already
            if(a.containsKey(temp)) {
                return true;
            }
        }
        return false;
    }

    public void writeToOutput(String text, String outputLocation) {
        try {
            File file = new File(outputLocation);
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file, true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write(text + '\n');
            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String truncate(double value) {
        DecimalFormat df = new DecimalFormat("#.00");
        df.setRoundingMode(RoundingMode.DOWN);
        return df.format(value);
    }

    public void removeAnyEmptyNodes(Map<String, Integer> nodeList) {
        Iterator<Map.Entry<String, Integer>> it = nodeList.entrySet().iterator();

        //Go through nodeList and count if reference is 0. If so, eject node
        while(it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            if(entry.getValue() == 0) {
                it.remove();
            }
        }
    }
}