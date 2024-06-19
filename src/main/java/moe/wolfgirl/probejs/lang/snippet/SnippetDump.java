package moe.wolfgirl.probejs.lang.snippet;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the generation of snippets.
 */
public class SnippetDump {
    public List<Snippet> snippets = new ArrayList<>();

    public Snippet snippet(String name) {
        Snippet snippet = new Snippet(name);
        snippets.add(snippet);
        return snippet;
    }

    public void fromDocs() {
        ProbeJSPlugin.forEachPlugin(plugin -> plugin.addVSCodeSnippets(this));
    }


    public void writeTo(Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path)) {
            JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(writer);
            jsonWriter.setIndent("    ");

            JsonObject compiled = new JsonObject();

            for (Snippet snippet : snippets) {
                compiled.add(snippet.name, snippet.compile());
            }
            ProbeJS.GSON_WRITER.toJson(compiled, JsonObject.class, jsonWriter);
        }
    }
}
