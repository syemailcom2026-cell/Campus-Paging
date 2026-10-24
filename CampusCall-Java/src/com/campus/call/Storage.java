package com.campus.call;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 本地持久化：学生名单、连接设置、呼叫记录。
 * 数据保存在用户目录下的 .campuscall 文件夹中。
 */
public class Storage {

    private static final Path DIR = Paths.get(System.getProperty("user.home"), ".campuscall");
    private static final Path ROSTER = DIR.resolve("roster.txt");
    private static final Path SETTINGS = DIR.resolve("settings.properties");
    private static final Path RECORDS = DIR.resolve("records.log");

    static {
        try {
            Files.createDirectories(DIR);
        } catch (IOException ignored) {
        }
    }

    public static String getDataDir() {
        return DIR.toString();
    }

    /** 读取学生名单，每行格式：班级,姓名 */
    public static List<String> loadRoster() {
        List<String> lines = new ArrayList<>();
        if (Files.exists(ROSTER)) {
            try {
                for (String l : Files.readAllLines(ROSTER, StandardCharsets.UTF_8)) {
                    l = l.trim();
                    if (!l.isEmpty()) {
                        lines.add(l);
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return lines;
    }

    /** 保存学生名单（整体覆盖） */
    public static void saveRoster(String text) {
        try {
            Files.write(ROSTER, text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    public static String loadSetting(String key, String def) {
        Properties p = loadProps();
        return p.getProperty(key, def);
    }

    public static void saveSetting(String key, String value) {
        Properties p = loadProps();
        p.setProperty(key, value);
        try (OutputStream out = Files.newOutputStream(SETTINGS)) {
            p.store(new OutputStreamWriter(out, StandardCharsets.UTF_8), "campus call settings");
        } catch (IOException ignored) {
        }
    }

    private static Properties loadProps() {
        Properties p = new Properties();
        if (Files.exists(SETTINGS)) {
            try (InputStream in = Files.newInputStream(SETTINGS)) {
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (IOException ignored) {
            }
        }
        return p;
    }

    public static void appendRecord(String line) {
        try {
            Files.write(RECORDS, (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    public static List<String> loadRecords() {
        List<String> lines = new ArrayList<>();
        if (Files.exists(RECORDS)) {
            try {
                lines.addAll(Files.readAllLines(RECORDS, StandardCharsets.UTF_8));
            } catch (IOException ignored) {
            }
        }
        return lines;
    }

    public static void clearRecords() {
        try {
            Files.deleteIfExists(RECORDS);
        } catch (IOException ignored) {
        }
    }
}
