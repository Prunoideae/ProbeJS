package moe.wolfgirl.probejs.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import moe.wolfgirl.probejs.ProbeJS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigUtils {
    // {
    //     "typescript.tsserver.maxTsServerMemory": 4096,
    //     "json.schemas": [
    //         {
    //             "fileMatch": [
    //                 "/recipe_schemas/*.json"
    //             ],
    //             "url": "./.vscode/recipe.json"
    //         }
    //     ]
    // }

    /**
     * Represents a VSCode config file.
     *
     * @param path file path to write the content to
     * @throws IOException if file operation errored
     */
    public static void writeVSCodeConfig(Path path) throws IOException {
        JsonObject config = Files.exists(path) ? ProbeJS.GSON.fromJson(Files.newBufferedReader(path), JsonObject.class) : new JsonObject();
        if (config == null) config = new JsonObject();

        // Update basic settings
        config.addProperty("typescript.tsserver.maxTsServerMemory", 4096);
        config.addProperty("typescript.disableAutomaticTypeAcquisition", true);
        config.addProperty("typescript.surveys.enabled", false);
        config.addProperty("typescript.validate.enable", false);


        // Handle the json.schemas array
        JsonArray schemasArray = JsonUtils.putArrayIfAbsent(config, "json.schemas");

        // Check if the required schema already exists
        boolean schemaExists = false;
        for (JsonElement element : schemasArray) {
            if (element.isJsonObject()) {
                JsonObject schema = element.getAsJsonObject();
                JsonElement urlElement = schema.get("url");
                JsonElement fileMatchElement = schema.get("fileMatch");

                if (urlElement != null && urlElement.isJsonPrimitive() &&
                        "./.vscode/recipe.json".equals(urlElement.getAsString())) {
                    if (fileMatchElement != null && fileMatchElement.isJsonArray()) {
                        JsonArray fileMatchArray = fileMatchElement.getAsJsonArray();
                        if (fileMatchArray.size() == 1) {
                            JsonElement fmElement = fileMatchArray.get(0);
                            if (fmElement.isJsonPrimitive() &&
                                    "/recipe_schemas/*.json".equals(fmElement.getAsString())) {
                                schemaExists = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Add the schema if it doesn't exist
        if (!schemaExists) {
            JsonObject newSchema = new JsonObject();
            JsonArray fileMatch = new JsonArray();
            fileMatch.add("/recipe_schema/*.json");
            newSchema.add("fileMatch", fileMatch);
            newSchema.addProperty("url", "./.vscode/recipe.json");
            schemasArray.add(newSchema);
        }

        // Write the updated configuration back to the file
        JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(Files.newBufferedWriter(path));
        jsonWriter.setIndent("    ");
        ProbeJS.GSON_WRITER.toJson(config, jsonWriter);
        jsonWriter.close();
    }

    private static void mergeAddArray(JsonArray jsonArray, JsonElement element) {
        for (JsonElement jsonElement : jsonArray) {
            if (jsonElement.equals(element)) return;
        }
        jsonArray.add(element);
    }


    public static void writeJSConfig(Path path, String baseName) throws IOException {
        JsonObject config = Files.exists(path) ? ProbeJS.GSON.fromJson(Files.newBufferedReader(path), JsonObject.class) : new JsonObject();
        if (config == null) config = new JsonObject();

        JsonArray include = JsonUtils.putArrayIfAbsent(config, "include");
        mergeAddArray(include, new JsonPrimitive("./**/*.js"));
        mergeAddArray(include, new JsonPrimitive("./**/*.ts"));

        JsonObject compilerOptions = JsonUtils.putObjectIfAbsent(config, "compilerOptions");
        compilerOptions.addProperty("module", "none");
        compilerOptions.addProperty("moduleResolution", "node");
        compilerOptions.addProperty("allowJs", true);
        compilerOptions.addProperty("checkJs", false);
        compilerOptions.addProperty("target", "ES2015");
        compilerOptions.addProperty("disableSizeLimit", true);
        compilerOptions.addProperty("noEmit", true);
        compilerOptions.addProperty("assumeChangesOnlyAffectDirectDependencies", true);

        compilerOptions.addProperty("rootDir", ".");
        compilerOptions.addProperty("baseUrl", "../../.probe/packages");
        compilerOptions.addProperty("skipLibCheck", true);
        compilerOptions.addProperty("skipDefaultLibCheck", true);


        JsonArray lib = JsonUtils.putArrayIfAbsent(compilerOptions, "lib");
        mergeAddArray(lib, new JsonPrimitive("ES2015"));
        mergeAddArray(lib, new JsonPrimitive("ES2016.Array.Include"));
        mergeAddArray(lib, new JsonPrimitive("ES2017.Object"));

        JsonArray typeRoots = JsonUtils.putArrayIfAbsent(compilerOptions, "typeRoots");
        mergeAddArray(typeRoots, new JsonPrimitive("../../.probe/packages"));
        mergeAddArray(typeRoots, new JsonPrimitive("../../.probe/%s".formatted(baseName)));

        // Write the updated configuration back to the file
        JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(Files.newBufferedWriter(path));
        jsonWriter.setIndent("    ");
        ProbeJS.GSON_WRITER.toJson(config, JsonObject.class, jsonWriter);
        jsonWriter.close();
    }
}
