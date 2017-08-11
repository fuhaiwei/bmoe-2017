package fuhaiwei.bmoe2017;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static fuhaiwei.bmoe2017.FileUtil.writeText;

public abstract class Handler {

    public static String handleData(JSONObject current, JSONArray data) {
        StringBuilder builder = new StringBuilder();
        builder.append(current.getString("title"));
        builder.append("\n\n");

        Map<Integer, Integer> votes = new HashMap<>();
        Map<Integer, Integer> loves = new HashMap<>();
        Map<Integer, String> nameMap = new LinkedHashMap<>();

        for (int i = 0; i < data.length(); i++) {
            JSONObject vote = data.getJSONObject(i);
            String[] characterIds = vote.getString("character_ids").split(",");
            if (vote.getInt("type") == 1) {
                Integer key = Integer.valueOf(characterIds[0]);
                loves.put(key, loves.getOrDefault(key, 0) + 1);
            } else {
                for (String characterId : characterIds) {
                    Integer key = Integer.valueOf(characterId);
                    votes.put(key, votes.getOrDefault(key, 0) + 1);
                }
            }
        }

        JSONArray voteGroups = current.getJSONArray("voteGroups");
        for (int i = 0; i < voteGroups.length(); i++) {
            JSONObject group = voteGroups.getJSONObject(i);
            String groupName = group.getString("group_name");
            builder.append("====");
            builder.append(groupName);
            builder.append("====");
            builder.append("\n");

            JSONArray characters = group.getJSONArray("characters");
            for (int j = 0; j < characters.length(); j++) {
                JSONObject character = characters.getJSONObject(j);
                Integer characterId = character.getInt("character_id");
                String chnName = character.getString("chn_name");
                nameMap.put(characterId, chnName);
                builder.append(chnName);
                builder.append("\n");
                int voteCount = votes.getOrDefault(characterId, 0);
                int loveCount = loves.getOrDefault(characterId, 0);
                builder.append("总票数: ").append(voteCount + loveCount * 2);
                builder.append("\n");
                builder.append("普通票: ").append(voteCount);
                builder.append("\n");
                builder.append("真爱票: ").append(loveCount);
                builder.append("\n\n");
            }
        }

        Map<String, Integer> voteMap = new LinkedHashMap<>();

        Integer[] characterIds = nameMap.keySet().toArray(new Integer[0]);
        for (int i = 0; i < characterIds.length; i++) {
            for (int j = i + 1; j < characterIds.length; j++) {
                Integer characterId1 = characterIds[i];
                Integer characterId2 = characterIds[j];
                for (int k = 0; k < data.length(); k++) {
                    JSONObject vote = data.getJSONObject(k);
                    String[] split = vote.getString("character_ids").split(",");
                    if (vote.getInt("type") == 0) {
                        int voteCount = 0;
                        for (String characterId : split) {
                            int id = Integer.parseInt(characterId);
                            if (id == characterId1) {
                                voteCount++;
                            }
                            if (id == characterId2) {
                                voteCount++;
                            }
                        }
                        if (voteCount == 2) {
                            String key = characterId1 + "," + characterId2;
                            voteMap.put(key, voteMap.getOrDefault(key, 0) + 1);
                        }
                    }
                }
            }
        }

        StringBuilder builder2 = new StringBuilder();
        builder2.append("====连记票分析====");
        builder2.append("\n");
        voteMap.forEach((k, v) -> handleRow(nameMap, builder2, k, v));
        writeText(builder2.toString(), new File("output/output.txt"));

        Map<String, Integer> treeMap = new TreeMap<>((k1, k2) -> voteMap.get(k2) - voteMap.get(k1));
        treeMap.putAll(voteMap);

        StringBuilder builder3 = new StringBuilder();
        builder3.append("====连记票分析====");
        builder3.append("\n");
        treeMap.forEach((k, v) -> handleRow(nameMap, builder3, k, v));
        writeText(builder3.toString(), new File("output/output-sort.txt"));

        return builder.toString();
    }

    private static void handleRow(Map<Integer, String> nameMap, StringBuilder builder2, String k, Integer v) {
        String[] split = k.split(",");
        String name1 = nameMap.get(Integer.parseInt(split[0]));
        String name2 = nameMap.get(Integer.parseInt(split[1]));
        builder2.append(name1);
        builder2.append(" + ");
        builder2.append(name2);
        builder2.append(" = ");
        builder2.append(v);
        builder2.append("\n");
    }

}
