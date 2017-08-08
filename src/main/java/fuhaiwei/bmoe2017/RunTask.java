package fuhaiwei.bmoe2017;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RunTask {

    private static final DateTimeFormatter DATE_FORMATTER;
    private static final DateTimeFormatter DATE_TIME_FORMATTER;

    static {
        DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd/yyyy-MM-dd HH:mm:ss");
    }

    public static void main(String[] args) {
        JSONObject current = fetchCurrent();
        if (current != null) {
            String date = DATE_FORMATTER.format(LocalDateTime.now());
            writeText(current.toString(), new File(String.format("bmoe-json/%s/current.txt", date)));

            JSONArray data = fetchData();
            String dataText = data.toString();

            String datetime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
            writeText(dataText, new File(String.format("bmoe-json/%s.txt", datetime)));

            String output = handleData(current, data);
            writeText(output, new File(String.format("bmoe-data/%s.txt", datetime)));
        }
    }

    private static String handleData(JSONObject current, JSONArray data) {
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

    private static String readText(String pathname) throws IOException {
        return Files.readAllLines(new File(pathname).toPath()).get(0);
    }

    private static void writeText(String dataText, File file) {
        file.getParentFile().mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(dataText);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("文件写入失败: " + file.getPath(), e);
        }

        System.out.println("文件写入成功: " + file.getName());
    }

    private static JSONObject fetchCurrent() {
        String url = "http://bangumi.bilibili.com/moe/2017/2/api/schedule/current";
        int err = 0;
        while (true) {
            try {
                String jsonText = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .timeout(3000)
                        .execute().body();
                JSONObject root = new JSONObject(jsonText);
                if (root.getInt("code") == 0 && "success".equals(root.getString("message"))) {
                    JSONObject result = root.getJSONObject("result");
                    JSONObject object = new JSONObject();
                    object.put("title", result.getString("title"));
                    object.put("voteGroups", result.getJSONArray("voteGroups"));
                    return object;
                } else {
                    break;
                }
            } catch (IOException e) {
                if (++err > 10) {
                    System.out.println("失败次数大于10次，链接为：" + url);
                    e.printStackTrace();
                    break;
                }
            }
        }
        return null;
    }

    private static JSONArray fetchData() {
        int threadCount = 5;
        AtomicInteger count = new AtomicInteger(threadCount);
        JSONArray array = new JSONArray();
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            new Thread(() -> {
                JSONArray data = fetchData(finalI, threadCount);
                synchronized (array) {
                    data.forEach(array::put);
                    if (count.decrementAndGet() == 0) {
                        array.notify();
                    }
                }
            }).start();
        }
        synchronized (array) {
            try {
                array.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    private static JSONArray fetchData(int start, int step) {
        String url = "http://bangumi.bilibili.com/moe/2017/2/api/realtime/votes?page=%d&pagesize=50";
        JSONArray array = new JSONArray();
        int page = start;
        int err = 0;
        while (true) {
            try {
                String date = DATE_FORMATTER.format(LocalDateTime.now());
                String pathname = String.format("bmoe-json/%s/page/%05d.txt", date, page);
                File file = new File(pathname);
                String jsonText;
                if (!file.exists() || !isFullData(jsonText = readText(pathname))) {
                    jsonText = Jsoup.connect(String.format(url, page))
                            .ignoreContentType(true)
                            .timeout(3000)
                            .execute().body();
                    writeText(jsonText, file);
                    System.out.printf("已成功获取数据[page=%d,errpr=%d]%n", page, err);
                } else {
                    System.out.printf("已读取现有数据[page=%d]%n", page);
                }
                JSONObject root = new JSONObject(jsonText);
                if (root.getInt("code") == 0 && "success".equals(root.getString("message"))) {
                    JSONArray result = root.getJSONArray("result");
                    int length = result.length();

                    if (length == 0) {
                        break;
                    }
                    for (int i = 0; i < length; i++) {
                        array.put(result.getJSONObject(i));
                    }
                } else {
                    break;
                }
                page += step;
                err = 0;
            } catch (IOException e) {
                if (++err > 10) {
                    System.out.println("失败次数大于10次，链接为：" + String.format(url, page));
                    e.printStackTrace();
                    break;
                }
            }
        }
        return array;
    }

    private static boolean isFullData(String jsonText) {
        JSONObject root = new JSONObject(jsonText);
        if (root.getInt("code") == 0 && "success".equals(root.getString("message"))) {
            if (root.getJSONArray("result").length() == 50) {
                return true;
            }
        }
        return false;
    }

}