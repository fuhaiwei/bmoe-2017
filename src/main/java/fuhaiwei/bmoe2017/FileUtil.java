package fuhaiwei.bmoe2017;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

public abstract class FileUtil {

    public static String readText(String pathname) throws IOException {
        return Files.readAllLines(new File(pathname).toPath()).get(0);
    }

    public static void writeText(String dataText, File file) {
        file.getParentFile().mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(dataText);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("文件写入失败: " + file.getPath(), e);
        }

        System.out.println("文件写入成功: " + file.getName());
    }

}
