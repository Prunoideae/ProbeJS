package moe.wolfgirl.probejs.dumps;

import dev.latvian.mods.kubejs.script.ScriptType;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.snippet.SnippetRegistry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

// Writes down jsconfig.json, .vscode/settings.json, snippets, etc.
public class ProjectDump {
    private final Path baseDir;

    public ProjectDump(Path baseDir) {
        this.baseDir = baseDir;
    }

    public void dump() throws IOException {
        GameStates.DUMP_STATE.setStatus(Component.literal("Dumping snippets..."));
        SnippetRegistry registry = new SnippetRegistry();
        ProbeJSPlugin.forEachWithPriority("addSnippets", plugin -> plugin.addSnippets(new SnippetRegistry.Proxy(registry)));
        registry.writeTo(baseDir.resolve(".vscode/probejs.code-snippets"));
        GameStates.DUMP_STATE.incrementProgress(1);

        GameStates.DUMP_STATE.setStatus(Component.literal("Writing configs..."));
        writeJsConfig(ScriptType.CLIENT);
        writeJsConfig(ScriptType.SERVER);
        writeJsConfig(ScriptType.STARTUP);
        GameStates.DUMP_STATE.incrementProgress(1);
    }

    private void writeJsConfig(@NotNull ScriptType scriptType) {
        var sideString = switch (scriptType) {
            case CLIENT -> "client";
            case SERVER -> "server";
            case STARTUP -> "startup";
            case null -> throw new IllegalStateException("Unexpected value: " + scriptType);
        };

        try {
            List<String> lines = readDumpFile("assets/probejs/dumps/jsconfig.jsonc", Map.of("side", sideString));
            var outputPath = baseDir.resolve("kubejs/%s_scripts/jsconfig.json".formatted(sideString));
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> readDumpFile(String resourcePath, Map<String, String> formatter) throws IOException {
        var modFile = ProbeJS.MOD_CONTAINER.getModInfo().getOwningFile().getFile();
        var resourceFile = modFile.findResource(resourcePath);
        if (!Files.exists(resourceFile)) throw new FileNotFoundException("Resource not found: " + resourcePath);

        return Files.readAllLines(resourceFile).stream().map(line -> {
            for (var entry : formatter.entrySet()) {
                line = line.replace("$%s$".formatted(entry.getKey()), entry.getValue());
            }
            return line;
        }).toList();
    }
}
