package moe.wolfgirl.probejs;

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
    public static Path WORKSPACE_SETTINGS = Platform.getGameFolder().resolve(".vscode");
    public static Path SETTINGS_JSON = WORKSPACE_SETTINGS.resolve("settings.json");

    public static Path DECOMPILED = PROBE.resolve("decompiled");
    public static Path TYPES = PROBE.resolve("probe-types");
    public static Path PACKAGES = TYPES.resolve("packages");

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
        if (Files.notExists(WORKSPACE_SETTINGS)) {
            UtilsJS.tryIO(() -> Files.createDirectories(WORKSPACE_SETTINGS));
        }
        if (Files.notExists(ProbePaths.PACKAGES)) {
            UtilsJS.tryIO(() -> Files.createDirectories(ProbePaths.PACKAGES));
        }
        if (Files.notExists(ProbePaths.TYPES)) {
            UtilsJS.tryIO(() -> Files.createDirectories(ProbePaths.TYPES));
        }
        if (Files.notExists(ProbePaths.DECOMPILED)) {
            UtilsJS.tryIO(() -> Files.createDirectories(ProbePaths.DECOMPILED));
        }
    }

    static {
        init();
    }
}
