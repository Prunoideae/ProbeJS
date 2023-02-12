package com.probejs;

import com.google.gson.GsonBuilder;
import dev.latvian.mods.kubejs.KubeJSPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ProbeConfig {
    public static ProbeConfig INSTANCE = new ProbeConfig();
    private static final Path CONFIG = KubeJSPaths.CONFIG.resolve("probejs.json");
    public boolean dumpMethod = true;
    public boolean noAggressiveProbing = false;
    public boolean vanillaOrder = true;
    public boolean exportClassNames = false;

    public boolean allowObfuscated = false;
    public long docsTimestamp = 0;

    public boolean allowRegistryObjectDumps = true;
    public boolean requireSingleAndPerm = true;

    @SuppressWarnings("unchecked")
    private static <E> E fetchPropertyOrDefault(Object key, Map<?, ?> value, E defaultValue) {
        Object v = value.get(key);
        return v == null ? defaultValue : (E) v;
    }

    private ProbeConfig() {
        Path cfg = KubeJSPaths.CONFIG.resolve("probejs.json");
        if (Files.exists(cfg)) {
            try {
                Map<?, ?> obj = ProbeJS.GSON.fromJson(Files.newBufferedReader(cfg), Map.class);
                dumpMethod = fetchPropertyOrDefault("dumpMethod", obj, true);
                noAggressiveProbing = fetchPropertyOrDefault("disabled", obj, false);
                vanillaOrder = fetchPropertyOrDefault("vanillaOrder", obj, true);
                exportClassNames = fetchPropertyOrDefault("exportClassNames", obj, false);
                allowObfuscated = fetchPropertyOrDefault("allowObfuscated", obj, false);
                docsTimestamp = fetchPropertyOrDefault("docsTimestamp", obj, 0D).longValue();
                allowRegistryObjectDumps = fetchPropertyOrDefault("allowRegistryObjectDumps", obj, true);
                requireSingleAndPerm = fetchPropertyOrDefault("requireSingleAndPerm", obj, true);
            } catch (IOException e) {
                ProbeJS.LOGGER.warn("Cannot read config properties, falling back to defaults.");
            }
        }
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(this, writer);
        } catch (IOException e) {
            ProbeJS.LOGGER.warn("Cannot write config, settings are not saved.");
        }
    }
}
