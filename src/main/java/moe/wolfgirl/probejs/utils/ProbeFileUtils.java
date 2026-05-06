package moe.wolfgirl.probejs.utils;

import java.nio.file.Files;
import java.nio.file.Path;

public class ProbeFileUtils {

    public static void createDirectories(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
