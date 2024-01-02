package com.probejs.compiler;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.probejs.ProbeCommands;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.compiler.formatter.NameResolver;
import com.probejs.compiler.formatter.formatter.special.FormatterLang;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.util.RLHelper;
import com.probejs.util.json.JArray;
import com.probejs.util.json.JObject;
import com.probejs.util.json.JPrimitive;
import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.registry.KubeJSRegistries;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SchemaCompiler {

    public static JsonObject toLangSchema() {
        return JObject.create()
                .add("type", JPrimitive.create("object"))
                .add("properties", Language.getInstance() instanceof ClientLanguage clientLanguage ?
                        JObject.create()
                                .addAll(FormatterLang.getLangKeys(LanguageManager.DEFAULT_LANGUAGE_CODE)
                                        .filter(e -> {
                                            var s = e.getKey();
                                            return !(s.startsWith("_") || s.startsWith("$"));
                                        })
                                        .map(entry -> new Pair<>(entry.getKey(),
                                                JObject.create()
                                                        .add("type", JPrimitive.create("string"))
                                                        .add("description", JPrimitive.create(entry.getValue()))))
                                )
                        : JObject.create()
                )
                .serialize();
    }

    public static JsonObject toClassDefinition(List<DocumentClass> mergedDocs) {
        return JObject.create()
                .add("definitions", JObject.create()
                        .add("typeClassName", JObject.create()
                                .add("enum", JArray.create()
                                        .addAll(mergedDocs.stream()
                                                .map(DocumentClass::getName)
                                                .map(JPrimitive::create))
                                        .addAll(NameResolver.resolvedPrimitives.stream().map(JPrimitive::create))
                                )
                                .add("type", JPrimitive.create("string"))
                        )
                )
                .serialize();
    }

    public static JsonObject toLangKeyDefinition() {
        return JObject.create()
                .add("definitions", JObject.create()
                        .add("typeLangKey", JObject.create()
                                .add("type", JPrimitive.create("string"))
                                .add("enum", JArray.create()
                                        .addAll(
                                                Language.getInstance() instanceof ClientLanguage clientLanguage ?
                                                        FormatterLang.getLangKeys(LanguageManager.DEFAULT_LANGUAGE_CODE)
                                                                .map(Map.Entry::getKey)
                                                                .map(JPrimitive::create)
                                                        : Stream.empty())
                                )
                        )
                )
                .serialize();
    }

    public static <T> JObject toRegistryDefinition(ResourceKey<Registry<T>> key) {
        return JObject.create()
                .add("type", JPrimitive.create("string"))
                .add("enum", JArray.create()
                        .addAll(
                                KubeJSRegistries.genericRegistry(key)
                                        .getIds()
                                        .stream()
                                        .map(ResourceLocation::toString)
                                        .map(JPrimitive::create)
                        )
                );
    }

    @SuppressWarnings("unchecked")
    public static <T> void toRegistryDefinitions() throws IOException {
        JObject definitions = JObject.create();
        ProbeCommands.COMMAND_LEVEL.registryAccess().registries().forEach(entry -> {
            JObject schema = toRegistryDefinition((ResourceKey<Registry<T>>) entry.key());
            definitions.add("type%s".formatted(RLHelper.rlToTitle(entry.key().location().getPath())), schema);
        });
        compileSchema("probe.registry-definitions.json", definitions.serialize());
    }

    private static void compileSchema(String fileName, JsonObject schema) throws IOException {
        Path schemaPath = ProbePaths.WORKSPACE_SETTINGS.resolve(fileName);
        BufferedWriter writer = Files.newBufferedWriter(schemaPath);
        writer.write(ProbeJS.GSON.toJson(schema));
        writer.close();
    }

    public static void compile(List<DocumentClass> mergedDocs) throws IOException {
        compileSchema("probe.lang-schema.json", SchemaCompiler.toLangSchema());
        compileSchema("probe.class-definitions.json", SchemaCompiler.toClassDefinition(mergedDocs));
        compileSchema("probe.lang_key-definitions.json", SchemaCompiler.toLangKeyDefinition());
        SchemaCompiler.toRegistryDefinitions();
        Path schemaDoc = ProbePaths.WORKSPACE_SETTINGS.resolve("probe.doc-schema.json");
        Platform.getMod("probejs").findResource("probe.doc-schema.json").ifPresent(path -> {
            try {
                Files.copy(path, schemaDoc, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
