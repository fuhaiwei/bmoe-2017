package fuhaiwei.bmoe2017;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RunTask {

    public static final DateTimeFormatter DATE_FORMATTER;
    public static final DateTimeFormatter DATE_TIME_FORMATTER;

    static {
        DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd/yyyy-MM-dd HH:mm:ss");
    }

    public static void main(String[] args) {
        JSONObject current = Fetcher.fetchCurrent();
        if (current != null) {
            String date = DATE_FORMATTER.format(LocalDateTime.now());
            FileUtil.writeText(current.toString(), new File(String.format("bmoe-json/%s/current.txt", date)));

            JSONArray data = Fetcher.fetchData();
            String dataText = data.toString();

            String datetime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
            FileUtil.writeText(dataText, new File(String.format("bmoe-json/%s.txt", datetime)));

            String output = Handler.handleData(current, data);
            FileUtil.writeText(output, new File(String.format("bmoe-data/%s.txt", datetime)));
        }
    }

}