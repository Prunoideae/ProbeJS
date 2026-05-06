package moe.wolfgirl.probejs.snippet;

import com.google.gson.JsonObject;
import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.probejs.ProbeJS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SnippetRegistry implements SnippetRegisterer {
    private final List<Snippet> snippets = new ArrayList<>();

    @Override
    public void register(Snippet snippet) {
        snippets.add(snippet);
    }

    @HideFromJS
    public void writeTo(Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path)) {
            var jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(writer);
            jsonWriter.setIndent("    ");
            var compiled = new JsonObject();
            for (Snippet snippet : snippets) {
                compiled.add(snippet.name, snippet.compile());
            }
            ProbeJS.GSON_WRITER.toJson(compiled, JsonObject.class, jsonWriter);
        }
    }

    public static class Proxy implements SnippetRegisterer {
        private final SnippetRegistry registry;

        public Proxy(SnippetRegistry registry) {
            this.registry = registry;
        }

        @Override
        public void register(Snippet snippet) {
            registry.register(snippet);
        }
    }
}
