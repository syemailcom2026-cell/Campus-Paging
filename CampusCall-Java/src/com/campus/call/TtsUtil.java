package com.campus.call;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 语音播报：调用 Windows 系统语音（SAPI）合成中文语音。
 * 异步执行，不阻塞界面；同一时刻只播报一条。
 */
public class TtsUtil {

    private static final ReentrantLock LOCK = new ReentrantLock();

    public static void speak(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        Thread t = new Thread(() -> {
            LOCK.lock();
            try {
                Path ps1 = Files.createTempFile("campuscall_tts", ".ps1");
                byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}; // UTF-8 BOM，保证 PowerShell 正确读取中文
                Files.write(ps1, bom);
                String script = "$v = New-Object -ComObject SAPI.SpVoice; $v.Rate = 0; $v.Volume = 100; $v.Speak('"
                        + escape(text) + "')";
                Files.write(ps1, script.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile",
                        "-ExecutionPolicy", "Bypass", "-File", ps1.toString());
                pb.redirectErrorStream(true);
                Process p = pb.start();
                p.waitFor();
                Files.deleteIfExists(ps1);
            } catch (Exception ignored) {
                // 语音播报失败不影响呼叫功能
            } finally {
                LOCK.unlock();
            }
        }, "tts-thread");
        t.setDaemon(true);
        t.start();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("'", "''");
    }
}
