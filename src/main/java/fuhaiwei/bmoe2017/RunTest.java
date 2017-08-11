package fuhaiwei.bmoe2017;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import static fuhaiwei.bmoe2017.FileUtil.readText;
import static fuhaiwei.bmoe2017.Handler.handleData;

public class RunTest {

    public static void main(String[] args) {
        try {
            System.out.println(handleData(
                    new JSONObject(readText("bmoe-json/2017-08-10/current.txt")),
                    new JSONArray(readText("bmoe-json/2017-08-10/2017-08-10 23:10:09.txt"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done!");
    }

}