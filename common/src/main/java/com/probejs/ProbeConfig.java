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
    private boolean noAggressiveProbing = false;
    public long docsTimestamp = 0;
    public String modHash = "0";
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
                noAggressiveProbing = fetchPropertyOrDefault("disabled", obj, false);
                docsTimestamp = fetchPropertyOrDefault("docsTimestamp", obj, 0D).longValue();
                modHash = fetchPropertyOrDefault("modHash", obj, "0");
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
        save();
        return noAggressiveProbing;
    }

    public boolean shouldProbingAggressive() {
        return !noAggressiveProbing && enabled;
    }
}
