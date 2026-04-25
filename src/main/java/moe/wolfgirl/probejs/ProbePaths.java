package moe.wolfgirl.probejs;

import dev.latvian.mods.kubejs.KubeJSPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProbePaths {

    public static Path PROBE = KubeJSPaths.GAMEDIR.resolve(".probe");
    public static Path WORKSPACE_SETTINGS = KubeJSPaths.GAMEDIR.resolve(".vscode");
    public static Path SETTINGS_JSON = KubeJSPaths.CONFIG.resolve("probe-settings.json");
    public static Path VSCODE_JSON = WORKSPACE_SETTINGS.resolve("settings.json");
    public static Path REGISTRY_BACKUPS = PROBE.resolve("registry_objects.json");
    public static Path GIT_IGNORE = KubeJSPaths.GAMEDIR.resolve(".gitignore");
    public static Path DECOMPILED = PROBE.resolve("decompiled");
    public static Path GENERATED_CODE = KubeJSPaths.EXPORT.resolve("generated_code");

    public static Path IMAGES = PROBE.resolve("images");

    // ProbeJS-Next
    public static Path PACKAGES = PROBE.resolve("@package");
    public static Path SIDE_SPECIFIC = PROBE.resolve("@side_specific");
    public static Path SPECIAL = PROBE.resolve("@special");

    public static void init() {
        createFolders(PROBE);
        createFolders(WORKSPACE_SETTINGS);
        createFolders(DECOMPILED);
        createFolders(IMAGES);
        createFolders(GENERATED_CODE);

        createFolders(PACKAGES);
        createFolders(SIDE_SPECIFIC);
        createFolders(SPECIAL);
    }

    private static void createFolders(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static {
        init();
    }
}
