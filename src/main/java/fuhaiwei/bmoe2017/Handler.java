package fuhaiwei.bmoe2017;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class Handler {

    public static String handleData(JSONObject current, JSONArray data) {
        StringBuilder builder = new StringBuilder();
        builder.append(current.getString("title"));
        builder.append("\n\n");

        Map<Integer, Integer> votes = new HashMap<>();
        Map<Integer, Integer> loves = new HashMap<>();

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

        return builder.toString();
    }

    public static boolean isFullData(String jsonText) {
        JSONObject root = new JSONObject(jsonText);
        if (root.getInt("code") == 0 && "success".equals(root.getString("message"))) {
            if (root.getJSONArray("result").length() == 50) {
                return true;
            }
        }
        return false;
    }

}
