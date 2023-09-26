package com.probejs;

import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.util.UtilsJS;

import java.nio.file.Files;
import java.nio.file.Path;

public class ProbePaths {

    public static Path PROBE = KubeJSPaths.DIRECTORY.resolve("probe");
    public static Path DOCS = PROBE.resolve("docs");
    public static Path GENERATED = PROBE.resolve("generated");
    public static Path INTERNALS = GENERATED.resolve("internals");
    public static Path USER_DEFINED = PROBE.resolve("user");
    public static Path WORKSPACE_SETTINGS = Platform.getGameFolder().resolve(".vscode");
    public static Path CACHE = PROBE.resolve("cache");
    public static Path RICH = CACHE.resolve("rich");

    public static Path RICH_ITEM = RICH.resolve("item");
    public static Path RICH_FLUID = RICH.resolve("fluid");

    public static Path KUBE_ASSETS = KubeJSPaths.DIRECTORY.resolve("assets");

    public static void init() {
        if (Files.notExists(PROBE)) {
            UtilsJS.tryIO(() -> Files.createDirectories(PROBE));
        }
        if (Files.notExists(DOCS)) {
            UtilsJS.tryIO(() -> Files.createDirectories(DOCS));
        }
        if (Files.notExists(GENERATED)) {
            UtilsJS.tryIO(() -> Files.createDirectories(GENERATED));
        }
        if (Files.notExists(INTERNALS)) {
            UtilsJS.tryIO(() -> Files.createDirectories(INTERNALS));
        }
        if (Files.notExists(USER_DEFINED)) {
            UtilsJS.tryIO(() -> Files.createDirectories(USER_DEFINED));
        }
        if (Files.notExists(WORKSPACE_SETTINGS)) {
            UtilsJS.tryIO(() -> Files.createDirectories(WORKSPACE_SETTINGS));
        }
        if (Files.notExists(CACHE)) {
            UtilsJS.tryIO(() -> Files.createDirectories(CACHE));
        }
        if (Files.notExists(RICH)) {
            UtilsJS.tryIO(() -> Files.createDirectories(RICH));
        }
    }

    static {
        init();
    }
}
