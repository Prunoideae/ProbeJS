package com.probejs;

import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.util.UtilsJS;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class ProbePaths {

    public static Path PROBE = KubeJSPaths.DIRECTORY.resolve("probe");
    public static Path DOCS = PROBE.resolve("docs");
    public static Path GENERATED = PROBE.resolve("generated");
    public static Path USER_DEFINED = PROBE.resolve("user");
    public static Path SNIPPET = Platform.getGameFolder().resolve(".vscode");

    public static void init() {
        if (Files.notExists(PROBE, new LinkOption[0])) {
            UtilsJS.tryIO(() -> Files.createDirectories(PROBE));
        }
        if (Files.notExists(DOCS, new LinkOption[0])) {
            UtilsJS.tryIO(() -> Files.createDirectories(DOCS));
        }
        if (Files.notExists(GENERATED, new LinkOption[0])) {
            UtilsJS.tryIO(() -> Files.createDirectories(GENERATED));
        }
        if (Files.notExists(USER_DEFINED, new LinkOption[0])) {
            UtilsJS.tryIO(() -> Files.createDirectories(USER_DEFINED));
        }
        if (Files.notExists(SNIPPET, new LinkOption[0])) {
            UtilsJS.tryIO(() -> Files.createDirectories(SNIPPET));
        }
    }

    static {
        init();
    }
}
