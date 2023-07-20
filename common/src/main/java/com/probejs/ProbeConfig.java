package com.probejs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.KubeJSPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ProbeConfig {
    public static ProbeConfig INSTANCE = new ProbeConfig();
    private static final int CONFIG_VERSION = 1;
    public boolean firstLoad = true;
    public boolean noAggressiveProbing = false;
    public long docsTimestamp = 0;
    public boolean allowRegistryObjectDumps = false;
    public boolean requireSingleAndPerm = true;
    public boolean enabled = true;

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
                firstLoad = fetchPropertyOrDefault("firstLoad", obj, true);
                noAggressiveProbing = fetchPropertyOrDefault("noAggressiveProbing", obj, false);
                docsTimestamp = fetchPropertyOrDefault("docsTimestamp", obj, 0D).longValue();
                allowRegistryObjectDumps = fetchPropertyOrDefault("allowRegistryObjectDumps", obj, false);
                requireSingleAndPerm = fetchPropertyOrDefault("requireSingleAndPerm", obj, true);
                enabled = fetchPropertyOrDefault("enabled", obj, true);
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
            JsonObject jObj = gson.toJsonTree(this).getAsJsonObject();
            jObj.addProperty("version", CONFIG_VERSION);
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
