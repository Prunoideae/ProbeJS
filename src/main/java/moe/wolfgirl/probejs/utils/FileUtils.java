package moe.wolfgirl.probejs.utils;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import dev.latvian.mods.kubejs.KubeJSPaths;
import moe.wolfgirl.probejs.ProbeJS;

import javax.annotation.Nullable;
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

    public static void writeMergedConfig(Path path, String config) throws IOException {
        JsonObject updates = ProbeJS.GSON.fromJson(config, JsonObject.class);
        JsonObject read = Files.exists(path) ? ProbeJS.GSON.fromJson(Files.newBufferedReader(path), JsonObject.class) : new JsonObject();
        if (read == null) read = new JsonObject();
        JsonObject original = (JsonObject) JsonUtils.mergeJsonRecursively(read, updates);
        JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(Files.newBufferedWriter(path));
        jsonWriter.setIndent("    ");
        ProbeJS.GSON_WRITER.toJson(original, JsonObject.class, jsonWriter);
        jsonWriter.close();
    }

    @Nullable
    public static Path parseSourcePath(String name) {
        if (!name.contains(":")) return null;
        String[] parts = name.split(":", 2);
        Path base = switch (parts[0]) {
            case "client_scripts" -> KubeJSPaths.CLIENT_SCRIPTS;
            case "server_scripts" -> KubeJSPaths.SERVER_SCRIPTS;
            case "startup_scripts" -> KubeJSPaths.STARTUP_SCRIPTS;
            case null, default -> null;
        };
        if (base == null) return null;
        return base.resolve(parts[1]);
    }
}
