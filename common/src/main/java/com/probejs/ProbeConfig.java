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
    public boolean dumpMethod = true;
    public boolean noAggressiveProbing = false;
    public boolean vanillaOrder = true;
    public boolean exportClassNames = false;

    public boolean allowObfuscated = false;
    public long docsTimestamp = 0;
    public String modHash = "0";

    public boolean allowRegistryObjectDumps = false;
    public boolean requireSingleAndPerm = true;

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
                dumpMethod = fetchPropertyOrDefault("dumpMethod", obj, true);
                noAggressiveProbing = fetchPropertyOrDefault("disabled", obj, false);
                vanillaOrder = fetchPropertyOrDefault("vanillaOrder", obj, true);
                exportClassNames = fetchPropertyOrDefault("exportClassNames", obj, false);
                allowObfuscated = fetchPropertyOrDefault("allowObfuscated", obj, false);
                docsTimestamp = fetchPropertyOrDefault("docsTimestamp", obj, 0D).longValue();
                modHash = fetchPropertyOrDefault("modHash", obj, "0");
                allowRegistryObjectDumps = fetchPropertyOrDefault("allowRegistryObjectDumps", obj, false);
                requireSingleAndPerm = fetchPropertyOrDefault("requireSingleAndPerm", obj, true);
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
}
