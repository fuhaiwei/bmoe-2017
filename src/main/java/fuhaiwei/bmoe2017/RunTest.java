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
                    new JSONObject(readText("/Users/fuhaiwei/workspace/bmoe-2017/bmoe-json/2017-08-09/current.txt")),
                    new JSONArray(readText("/Users/fuhaiwei/workspace/bmoe-2017/bmoe-json/2017-08-09/2017-08-09 23:10:09.txt"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done!");
    }

}