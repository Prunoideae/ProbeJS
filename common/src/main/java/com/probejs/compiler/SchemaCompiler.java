package com.probejs.compiler;

import com.google.gson.JsonObject;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SchemaCompiler {

    public static JsonObject toSchema() {
        JsonObject properties = new JsonObject();
        if (Language.getInstance() instanceof ClientLanguage clientLanguage) {
            clientLanguage.storage
                    .keySet()
                    .stream().filter(s -> !(s.startsWith("_") || s.startsWith("$")))
                    .forEach(key -> {
                        JsonObject typeString = new JsonObject();
                        typeString.addProperty("type", "string");
                        properties.add(key, typeString);
                    });
        }
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", properties);
        return schema;
    }

    public static void compile() throws IOException {
        Path schemaFile = ProbePaths.SCHEMA.resolve("schema.json");
        BufferedWriter writer = Files.newBufferedWriter(schemaFile);
        writer.write(ProbeJS.GSON.toJson(SchemaCompiler.toSchema()));
        writer.flush();
    }
}
