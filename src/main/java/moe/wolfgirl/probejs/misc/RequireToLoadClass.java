package moe.wolfgirl.probejs.misc;

import dev.latvian.mods.kubejs.KubeJSPaths;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequireToLoadClass {
    // Matches: const { $ClassName } = require("path")
    // Or:      const { $ClassName: Alias } = require("path")
    private static final Pattern REQUIRE_PATTERN = Pattern.compile(
            "const\\s*\\{\\s*(\\$[\\w$]+)(?:\\s*:\\s*(\\w+))?\\s*}\\s*=\\s*require\\(\"([^\"]+)\"\\)\\s*;?"
    );

    /**
     * Processes a single line of code, converting require() statements to Java.loadClass() where applicable.
     *
     * @param line The line of code to process.
     * @return The converted line if it was a require statement, or the original line if not.
     * @author DeepSeek v4 Pro
     */
    private static String processLine(String line) {
        Matcher matcher = REQUIRE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return line; // not a require statement, no conversion needed
        }

        String className = matcher.group(1);  // e.g. "$Item"
        String alias = matcher.group(2);       // e.g. "MyItem" (nullable)
        String tsPath = matcher.group(3);      // e.g. "@package/net/minecraft/world/item"

        // Process the TS path the same way Require.java does
        var segments = List.of(tsPath.split("/"));
        if (segments.size() < 2 || !segments.getFirst().equals("@package")) {
            return line; // invalid TS path for loading a Java class, no conversion
        }

        var classSegments = segments.subList(1, segments.size());
        var basePath = new ClassPath(classSegments);

        // append(className) keeps the $ prefix; asJavaPath() strips it from the last segment
        String javaPath = basePath.append(className).asJavaPath();
        String variableName = alias != null ? alias : className;

        return "let %s = Java.loadClass(\"%s\");".formatted(variableName, javaPath);
    }

    public static void recursiveConvert(Path directory) throws IOException {
        try (var stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".js"))
                    .forEach(p -> {
                        try {
                            List<String> lines = Files.readAllLines(p).stream().map(RequireToLoadClass::processLine).toList();
                            Files.write(p, lines);
                        } catch (IOException e) {
                            GameUtils.logException(e);
                        }
                    });
        }
    }

    public static void convertScripts() {
        try {
            recursiveConvert(KubeJSPaths.STARTUP_SCRIPTS);
            recursiveConvert(KubeJSPaths.SERVER_SCRIPTS);
            recursiveConvert(KubeJSPaths.CLIENT_SCRIPTS);
            if (GameStates.DUMP_SCREEN != null) {
                GameStates.DUMP_SCREEN.setStatus(Component.literal("Done!"));
            }
        } catch (Exception e) {
            if (GameStates.DUMP_SCREEN != null) {
                GameStates.DUMP_SCREEN.setStatus(Component.literal("Failed to convert!"));
            }
        }
    }
}
