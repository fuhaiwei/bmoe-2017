package fuhaiwei.bmoe2017;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static fuhaiwei.bmoe2017.FileUtil.*;
import static fuhaiwei.bmoe2017.Handler.handleData;

public class RunTest {

    public static void main(String[] args) {
        renameTo("bmoe-json", "data");
        renameTo("bmoe-data", "output");
        try {
            String output = handleData(
                    new JSONObject(readText("data/2017-08-10/current.txt")),
                    new JSONArray(readText("data/2017-08-10/2017-08-10 23:10:09.txt")));
            writeText(output, new File("output/output-test.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done!");
    }

}