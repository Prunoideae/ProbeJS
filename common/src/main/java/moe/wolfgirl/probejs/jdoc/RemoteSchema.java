package moe.wolfgirl.probejs.jdoc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.ProbePaths;
import moe.wolfgirl.probejs.util.json.JArray;
import moe.wolfgirl.probejs.util.json.JObject;
import moe.wolfgirl.probejs.util.json.JPrimitive;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;
import dev.latvian.mods.kubejs.recipe.schema.minecraft.SpecialRecipeSchema;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Downloads schema from a certain server.
 */
public class RemoteSchema {
    public static final String SCHEMA_DOWNLOAD = "https://api.wolfgirl.moe/schemas/download/%s".formatted(SharedConstants.getCurrentVersion().getName());
    public static final String SCHEMA_CHECK = "https://api.wolfgirl.moe/schemas/mods/%s".formatted(SharedConstants.getCurrentVersion().getName());

    public static Set<String> getSupportedMods() throws IOException {
        Set<String> supported = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(SCHEMA_CHECK).openStream()))) {
            JsonObject object = ProbeJS.GSON.fromJson(reader, JsonObject.class);
            for (JsonElement mod : object.get("mods").getAsJsonArray()) {
                supported.add(mod.getAsString());
            }
        }
        return supported;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getSchemas(Consumer<String> sendMessage) throws InterruptedException, IOException {
        var supported = getSupportedMods();
        var serializers = RegistryInfo.RECIPE_SERIALIZER.entrySet()
                .stream()
                .map(Map.Entry::getKey)
                .map(ResourceKey::location)
                .map(ResourceLocation::getNamespace)
                .filter(supported::contains)
                .collect(Collectors.toSet());
        for (Map.Entry<String, RecipeNamespace> entry : RecipeNamespace.getAll().entrySet()) {
            String key = entry.getKey();
            RecipeNamespace namespace = entry.getValue();
            for (Map.Entry<String, RecipeSchemaType> typeEntry : namespace.entrySet()) {
                RecipeSchemaType recipe = typeEntry.getValue();
                if (recipe.schema == SpecialRecipeSchema.SCHEMA) continue;
                // If the mod already has some sort of support
                // we skip it and let addon people be thrown in fire
                if (recipe.schema != JsonRecipeSchema.SCHEMA) {
                    serializers.remove(key);
                    break;
                }
            }
        }
        if (serializers.isEmpty()) return Map.of();
        sendMessage.accept("Found %s mods having schemas available, downloading...".formatted(serializers.size()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SCHEMA_DOWNLOAD))
                .POST(HttpRequest.BodyPublishers.ofString(ProbeJS.GSON.toJson(JObject.create()
                        .add("mods", JArray.create().addAll(serializers.stream().map(JPrimitive::new)))
                        .serialize())))
                .build();
        Map<? extends String, ? extends String> content = ProbeJS.GSON.fromJson(
                ProbeJS.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body(),
                Map.class
        );
        return new HashMap<>(content);
    }

    public static void dumpSchemas(Consumer<String> sendMessage) {
        try {
            Map<String, String> schemas = RemoteSchema.getSchemas(sendMessage);
            for (Map.Entry<String, String> entry : schemas.entrySet()) {
                String schema = entry.getKey();
                String content = entry.getValue();
                try (BufferedWriter writer = Files.newBufferedWriter(ProbePaths.SCHEMA.resolve(schema))) {
                    writer.write(content);
                    if (!schema.equals("prelude.js")) {
                        ProbeJS.LOGGER.info("Written schema file: %s".formatted(schema));
                    }
                }
            }
            if (!schemas.isEmpty()) {
                sendMessage.accept("Schema generation done. Restart to register all downloaded schemas.");
            }
        } catch (Exception e) {
            ProbeJS.LOGGER.warn("Failed to dump schemas");
            e.printStackTrace();
        }
    }
}
