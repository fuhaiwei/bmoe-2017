package fuhaiwei.bmoe2017;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static fuhaiwei.bmoe2017.Fetcher.fetchCurrent;
import static fuhaiwei.bmoe2017.Fetcher.fetchData;
import static fuhaiwei.bmoe2017.FileUtil.renameTo;
import static fuhaiwei.bmoe2017.FileUtil.writeText;
import static fuhaiwei.bmoe2017.Handler.handleData;

public class RunTask {

    public static final DateTimeFormatter DATE_FORMATTER;
    public static final DateTimeFormatter DATE_TIME_FORMATTER;

    static {
        DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd/yyyy-MM-dd HH:mm:ss");
    }

    public static void main(String[] args) {
        JSONObject current = fetchCurrent();
        if (current != null) {
            String date = DATE_FORMATTER.format(LocalDateTime.now());
            writeText(current.toString(), new File(String.format("data/%s/current.txt", date)));

            JSONArray data = fetchData();
            String dataText = data.toString();

            String datetime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
            writeText(dataText, new File(String.format("data/%s.txt", datetime)));

            String output = handleData(current, data);
            writeText(output, new File(String.format("output/%s.txt", datetime)));
        }
        System.out.println("Done!");
    }

}