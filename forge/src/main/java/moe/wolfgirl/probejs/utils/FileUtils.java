package moe.wolfgirl.probejs.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FileUtils {
    public static void forEachFile(Path basePath, Consumer<Path> callback) throws IOException {
        try (var dirStream = Files.newDirectoryStream(basePath)) {
            for (Path path : dirStream) {
                if (Files.isDirectory(path)) {
                    forEachFile(path, callback);
                } else {
                    callback.accept(path);
                }
            }
        }
    }
}
