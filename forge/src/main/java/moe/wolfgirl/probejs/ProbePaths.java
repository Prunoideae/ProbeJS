package moe.wolfgirl.probejs;

import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.util.UtilsJS;

import java.nio.file.Files;
import java.nio.file.Path;

public class ProbePaths {

    public static Path PROBE = Platform.getGameFolder().resolve(".probe");
    public static Path WORKSPACE_SETTINGS = Platform.getGameFolder().resolve(".vscode");
    public static Path SETTINGS_JSON = KubeJSPaths.CONFIG.resolve("probe-settings.json");
    public static Path DECOMPILED = PROBE.resolve("decompiled");

    public static void init() {
        if (Files.notExists(PROBE)) {
            UtilsJS.tryIO(() -> Files.createDirectories(PROBE));
        }
        if (Files.notExists(WORKSPACE_SETTINGS)) {
            UtilsJS.tryIO(() -> Files.createDirectories(WORKSPACE_SETTINGS));
        }
        if (Files.notExists(ProbePaths.DECOMPILED)) {
            UtilsJS.tryIO(() -> Files.createDirectories(ProbePaths.DECOMPILED));
        }
    }

    static {
        init();
    }
}
