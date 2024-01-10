package com.probejs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.KubeJSPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ProbeConfig {
    public static final int MOD_COUNT = 350;

    public static ProbeConfig INSTANCE = new ProbeConfig();
    private static final int CONFIG_VERSION = 2;
    public boolean firstLoad = true;
    private static final String FIRST_LOAD_KEY = "Is ProbeJS Loaded for First Time in the Modpack? - Configured by ProbeJS Itself";
    public boolean noAggressiveProbing = false;
    private static final String NO_AGGRESSIVE_PROBING_KEY = "Disable Aggressive Mode for ProbeJS Dumps";
    public long docsTimestamp = 0;
    private static final String DOCS_TIMESTAMP_KEY = "The Timestamp of ProbeJS Remote Documents - Configured by Mod Itself";
    public boolean allowRegistryObjectDumps = true;
    private static final String ALLOW_REGISTRY_OBJECT_DUMPS_KEY = "Allow ProbeJS to Resolve Classes from Registries Like Item Classes or Block Classes";
    public boolean allowRegistryLiteralDumps = true;
    private static final String ALLOW_REGISTRY_LITERAL_DUMPS_KEY = "Allow ProbeJS to Generate Literal Types for Item/Block/etc. IDs";
    public boolean requireSingleAndPerm = true;
    private static final String REQUIRE_SINGLE_AND_PERM_KEY = "Should ProbeJS Only Show Command in Single Player and with Cheat Enabled";
    public boolean enabled = true;
    private static final String ENABLED_KEY = "Should ProbeJS be Generally Enabled";
    public boolean modChanged = false;

    public boolean disableRecipeJsonDump = true;
    private static final String DISABLE_RECIPE_JSON_DUMP_KEY = "Disable the Recipe JSON Snippet Generation for ProbeJS triggered by `#`";
    public boolean dumpJSONIntermediates = false;
    private static final String DUMP_JSON_INTERMEDIATES_KEY = "Should ProbeJS Generate Intermediate JSON Representation of Documents - Mostly for Debugging";
    public boolean pullSchema = false;
    private static final String PULL_SCHEMA_KEY = "Should ProbeJS Download Schema Scripts from Github";

    @SuppressWarnings("unchecked")
    private static <E> E fetchPropertyOrDefault(Object key, Map<?, ?> value, E defaultValue) {
        if (value == null || !value.containsKey("version") || ((double) value.get("version")) < CONFIG_VERSION) {
            ProbeJS.LOGGER.warn("Config version has changed! Config values are rolled back to default.");
            return defaultValue;
        }
        Object v = value.get(key);
        return v == null ? defaultValue : (E) v;
    }

    private ProbeConfig() {
        var cfg = KubeJSPaths.CONFIG.resolve("probejs.json");
        if (Files.exists(cfg)) {
            try {
                Map<?, ?> obj = ProbeJS.GSON.fromJson(Files.newBufferedReader(cfg), Map.class);
                firstLoad = fetchPropertyOrDefault(FIRST_LOAD_KEY, obj, true);
                docsTimestamp = fetchPropertyOrDefault(DOCS_TIMESTAMP_KEY, obj, 0D).longValue();
                noAggressiveProbing = fetchPropertyOrDefault(NO_AGGRESSIVE_PROBING_KEY, obj, false);
                allowRegistryObjectDumps = fetchPropertyOrDefault(ALLOW_REGISTRY_OBJECT_DUMPS_KEY, obj, true);
                allowRegistryLiteralDumps = fetchPropertyOrDefault(ALLOW_REGISTRY_LITERAL_DUMPS_KEY, obj, true);
                requireSingleAndPerm = fetchPropertyOrDefault(REQUIRE_SINGLE_AND_PERM_KEY, obj, true);
                enabled = fetchPropertyOrDefault(ENABLED_KEY, obj, true);
                disableRecipeJsonDump = fetchPropertyOrDefault(DISABLE_RECIPE_JSON_DUMP_KEY, obj, true);
                dumpJSONIntermediates = fetchPropertyOrDefault(DUMP_JSON_INTERMEDIATES_KEY, obj, false);
                pullSchema = fetchPropertyOrDefault(PULL_SCHEMA_KEY, obj, false);
            } catch (IOException e) {
                ProbeJS.LOGGER.warn("Cannot read config properties, falling back to defaults.");
            }
        }
        save();
    }

    public void save() {
        var cfg = KubeJSPaths.CONFIG.resolve("probejs.json");
        try (BufferedWriter writer = Files.newBufferedWriter(cfg)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            JsonObject jObj = new JsonObject();
            jObj.addProperty("version", CONFIG_VERSION);
            jObj.addProperty(FIRST_LOAD_KEY, firstLoad);
            jObj.addProperty(DOCS_TIMESTAMP_KEY, docsTimestamp);
            jObj.addProperty(NO_AGGRESSIVE_PROBING_KEY, noAggressiveProbing);
            jObj.addProperty(ALLOW_REGISTRY_OBJECT_DUMPS_KEY, allowRegistryObjectDumps);
            jObj.addProperty(ALLOW_REGISTRY_LITERAL_DUMPS_KEY, allowRegistryLiteralDumps);
            jObj.addProperty(REQUIRE_SINGLE_AND_PERM_KEY, requireSingleAndPerm);
            jObj.addProperty(ENABLED_KEY, enabled);
            jObj.addProperty(DISABLE_RECIPE_JSON_DUMP_KEY, DISABLE_RECIPE_JSON_DUMP_KEY);
            jObj.addProperty(DUMP_JSON_INTERMEDIATES_KEY, DUMP_JSON_INTERMEDIATES_KEY);
            jObj.addProperty(PULL_SCHEMA_KEY, pullSchema);

            gson.toJson(jObj, writer);
        } catch (IOException e) {
            ProbeJS.LOGGER.warn("Cannot write config, settings are not saved.");
        }
    }

    public boolean toggleAggressiveProbing() {
        noAggressiveProbing = !noAggressiveProbing;
        return noAggressiveProbing;
    }

    public boolean shouldProbingAggressive() {
        return !noAggressiveProbing && enabled;
    }

    public static String getModHash() {
        Path hashFile = ProbePaths.CACHE.resolve("modHash.txt");
        if (Files.exists(hashFile)) {
            try {
                return Files.readString(hashFile);
            } catch (IOException e) {
                ProbeJS.LOGGER.warn("Cannot read mod hash file, falling back to default.");
            }
        }
        return "0";
    }

    public static void writeModHash(String hash) {
        Path hashFile = ProbePaths.CACHE.resolve("modHash.txt");
        try {
            Files.writeString(hashFile, hash);
        } catch (IOException e) {
            ProbeJS.LOGGER.warn("Cannot write mod hash file, settings are not saved.");
        }
    }
}
