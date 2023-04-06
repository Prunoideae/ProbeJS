package com.probejs.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.probejs.ProbeCommands;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.formatter.formatter.special.FormatterLootTable;
import com.probejs.formatter.formatter.special.FormatterTag;
import com.probejs.util.json.JArray;
import com.probejs.util.json.JObject;
import com.probejs.util.json.JPrimitive;
import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.KubeJSRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class SnippetCompiler {
    public static class KubeDump {
        public Map<String, Map<String, List<String>>> tags;
        public Map<String, List<String>> registries;

        public KubeDump(Map<String, Map<String, List<String>>> tags, Map<String, List<String>> registries) {
            this.tags = tags;
            this.registries = registries;
        }

        private static void putTag(Map<String, Map<String, List<String>>> tags, String type, Registry<?> registry) {
            tags.put(type, FormatterTag.getTagsFromRegistry(registry).stream().collect(Collectors.toMap(s -> s, s -> new ArrayList<>())));
        }

        private static <T> void putRegistry(Map<String, List<String>> registries, String type, ResourceKey<Registry<T>> registry) {
            registries.put(type, KubeJSRegistries.genericRegistry(registry).getIds().stream().map(ResourceLocation::toString).toList());
        }

        @SuppressWarnings("unchecked")
        public static <T> KubeDump fetch() {
            Map<String, Map<String, List<String>>> tags = new HashMap<>();
            Map<String, List<String>> registries = new HashMap<>();
            ProbeCommands.COMMAND_LEVEL.registryAccess().registries().forEach(registry -> {
                ResourceKey<? extends Registry<?>> key = registry.key();
                String[] paths = key.location().getPath().split("/");
                String name = paths[paths.length - 1];
                putTag(tags, name, registry.value());
                putRegistry(registries, name, (ResourceKey<Registry<T>>) key);
            });
            return new KubeDump(tags, registries);
        }

        @Override
        public String toString() {
            return "KubeDump{" +
                    "tags=" + tags +
                    ", registries=" + registries +
                    '}';
        }

        private static void addSnippets(JsonObject resultJson, String type, Collection<String> members) {
            if (!members.isEmpty()) {
                resultJson.add(type, new JObject()
                        .add("prefix", new JArray().add(new JPrimitive("@" + type)))
                        .add("body", new JPrimitive("\"${1|%s|}\"".formatted(String.join(",", members))))
                        .serialize()
                );
            }
        }

        public JsonObject toSnippet() {
            JsonObject resultJson = new JsonObject();
            // Compile normal entries to snippet
            for (Map.Entry<String, List<String>> entry : this.registries.entrySet()) {
                String type = entry.getKey();
                List<String> members = entry.getValue();
                addSnippets(resultJson, type, members);
            }

            // Compile tag entries to snippet
            for (Map.Entry<String, Map<String, List<String>>> entry : this.tags.entrySet()) {
                String type = entry.getKey();
                List<String> members = entry.getValue().keySet().stream().toList();
                addSnippets(resultJson, type + "_tag", members);
            }
            addSnippets(resultJson, "loot_table", FormatterLootTable.LOOT_TABLES.stream().map(ResourceLocation::toString).collect(Collectors.toList()));
            addSnippets(resultJson, "mod", Platform.getModIds());
            return resultJson;
        }

    }

    public static void compile() throws IOException {
        Path codeFile = ProbePaths.WORKSPACE_SETTINGS.resolve("probe.code-snippets");
        KubeDump kubeDump = KubeDump.fetch();
        BufferedWriter writer = Files.newBufferedWriter(codeFile);
        writer.write(ProbeJS.GSON.toJson(kubeDump.toSnippet()));
        writer.close();
    }

}
