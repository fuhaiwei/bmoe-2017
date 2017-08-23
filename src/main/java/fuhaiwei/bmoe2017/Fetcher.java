package fuhaiwei.bmoe2017;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static fuhaiwei.bmoe2017.RunTask.DATE_FORMATTER;

public abstract class Fetcher {

    public static JSONObject fetchCurrent() {
        String url = "http://bangumi.bilibili.com/moe/2017/2/api/schedule/current";
        int err = 0;
        while (true) {
            try {
                String jsonText = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .timeout(3000)
                        .execute()
                        .body();
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

    public static JSONArray fetchData() {
        int threadCount = 5;
        AtomicInteger count = new AtomicInteger(threadCount);
        JSONArray array = new JSONArray();
        for (int i = 0; i < threadCount; i++) {
            int finalI = i + 1;
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
                String pathname = String.format("data/%s/page/%05d.txt", date, page);
                File file = new File(pathname);
                String jsonText;
                if (!file.exists() || !isFullData(jsonText = FileUtil.readText(pathname))) {
                    jsonText = Jsoup.connect(String.format(url, page))
                            .ignoreContentType(true)
                            .timeout(3000)
                            .execute()
                            .body();
                    FileUtil.writeText(jsonText, file);
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
