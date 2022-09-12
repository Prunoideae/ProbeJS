package com.probejs.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.formatter.NameResolver;
import com.probejs.jdoc.document.DocumentClass;
import dev.architectury.platform.Platform;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class SchemaCompiler {

    public static JsonObject toLangSchema() {
        JsonObject properties = new JsonObject();
        if (Language.getInstance() instanceof ClientLanguage clientLanguage) {
            clientLanguage.storage
                    .entrySet()
                    .stream().filter(e -> {
                        var s = e.getKey();
                        return !(s.startsWith("_") || s.startsWith("$"));
                    })
                    .forEach(entry -> {
                        JsonObject typeString = new JsonObject();
                        typeString.addProperty("type", "string");
                        typeString.addProperty("description", entry.getValue());
                        properties.add(entry.getKey(), typeString);
                    });
        }
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", properties);
        return schema;
    }

    public static JsonObject toDocSchema(List<DocumentClass> mergedDocs) {
        JsonObject schema = new JsonObject();
        JsonArray enums = new JsonArray();
        mergedDocs.stream().map(DocumentClass::getName).forEach(enums::add);
        NameResolver.resolvedPrimitives.forEach(enums::add);
        JsonObject type = new JsonObject();
        type.add("enum", enums);
        type.addProperty("type", "string");
        JsonObject definition = new JsonObject();
        definition.add("typeClassname", type);
        schema.add("definitions", definition);
        return schema;
    }

    public static void compile(List<DocumentClass> mergedDocs) throws IOException {
        Path schemaLang = ProbePaths.WORKSPACE_SETTINGS.resolve("probe.lang-schema.json");
        BufferedWriter writerLang = Files.newBufferedWriter(schemaLang);
        writerLang.write(ProbeJS.GSON.toJson(SchemaCompiler.toLangSchema()));
        writerLang.flush();
        Path schemaClassNames = ProbePaths.WORKSPACE_SETTINGS.resolve("probe.class-definitions.json");
        BufferedWriter writerClazz = Files.newBufferedWriter(schemaClassNames);
        writerClazz.write(ProbeJS.GSON.toJson(SchemaCompiler.toDocSchema(mergedDocs)));
        writerClazz.flush();
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
