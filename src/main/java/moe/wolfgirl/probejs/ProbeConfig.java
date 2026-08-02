package moe.wolfgirl.probejs;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugins;
import moe.wolfgirl.probejs.utils.JsonUtils;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Read / write to .vscode/settings.json
 */
public class ProbeConfig {
    public static ProbeConfig INSTANCE = new ProbeConfig();

    public ConfigEntry<Boolean> enabled = new ConfigEntry<>("enabled", true);
    public ConfigEntry<Integer> recursionDepth = new ConfigEntry<>("recursionDepth", 5);
    // remove .probe, .vscode, .github, jsconfig.json, and package.json before dumping
    public ConfigEntry<Boolean> reset = new ConfigEntry<>("reset", true);
    // what mods are force-included from an incomplete dump, other mods/registry objects are stripped off
    public ConfigEntry<String> mods = new ConfigEntry<>("forceIncluded", "kubejs,minecraft,neoforge");
    public ConfigEntry<Boolean> beans = new ConfigEntry<>("generateBeans", true);
    public ConfigEntry<Boolean> hintsForLLM = new ConfigEntry<>("hintsForLLM", false);
    public ConfigEntry<List<String>> excludedPaths = new ConfigEntry<>("excludedClassPaths", List.of());
    public ConfigEntry<List<String>> fullScanMods = new ConfigEntry<>("fullScanMods", findFullScanMods());
    public ConfigEntry<Boolean> explicitNames = new ConfigEntry<>("explicitNames", false);
    public ConfigEntry<Boolean> newGame = new ConfigEntry<>("newGame", true);
    public ConfigEntry<List<String>> modSources = new ConfigEntry<>("modSources", List.of("https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.199/neoforge-21.1.199-sources.jar"));


    private static List<String> findFullScanMods() {
        Set<String> mods = new HashSet<>(List.of("minecraft", "kubejs", "neoforge"));
        // Fully load addon mods
        for (IModFileInfo modFileInfo : ModList.get().getModFiles()) {
            var modFile = modFileInfo.getFile();
            if (!modFile.getModInfos().isEmpty()) {
                var path = modFile.findResource("kubejs.plugins.txt");
                if (Files.exists(path)) {
                    for (IModInfo modInfo : modFile.getModInfos()) {
                        mods.add(modInfo.getModId());
                    }
                }
            }
        }
        return new ArrayList<>(mods);
    }

    public static class ConfigEntry<T> {
        public final String name;
        public final T defaultValue;
        private T value;
        private final String namespace;
        private boolean changed = true;

        ConfigEntry(String name, @Nonnull T defaultValue) {
            this(name, defaultValue, "probejs");
        }

        ConfigEntry(String name, @Nonnull T defaultValue, String namespace) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.namespace = namespace;
        }

        public void set(T value) {
            if (value == null) value = defaultValue;
            if (Objects.equals(this.value, value)) return;
            this.value = value;
            try {
                writeConfigEntry(this);
                changed = true;
            } catch (IOException ignored) {
            }
        }

        public T get() {
            try {
                if (changed) fromSetting();
                changed = false;
            } catch (IOException e) {
                return defaultValue;
            }
            return value == null ? defaultValue : value;
        }

        @SuppressWarnings("unchecked")
        private void fromSetting() throws IOException {
            Class<?> typeClass = defaultValue.getClass();
            Object configValue = getConfigEntry(this);

            if (configValue == null) value = null;
            else if (configValue instanceof Number number) {
                if (typeClass == Integer.class) configValue = number.intValue();
                if (typeClass == Float.class) configValue = number.floatValue();
                if (typeClass == Long.class) configValue = number.longValue();
                if (typeClass == Byte.class) configValue = number.byteValue();
                if (typeClass == Double.class) configValue = number.doubleValue();
                if (typeClass == Short.class) configValue = number.shortValue();
                value = (T) configValue;
            } else if (configValue instanceof List<?> list && List.class.isAssignableFrom(typeClass)) {
                value = (T) list;
            } else if (typeClass.isInstance(configValue)) {
                value = (T) configValue;
            }
        }
    }

    private static void writeConfigEntry(ConfigEntry<?> configEntry) throws IOException {
        String name = configEntry.name;
        Object value = configEntry.value;

        JsonObject current = new JsonObject();
        if (Files.exists(ProbePaths.SETTINGS_JSON)) {
            try (var reader = Files.newBufferedReader(ProbePaths.SETTINGS_JSON)) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                current = ProbeJS.GSON.fromJson(JsonUtils.stripSussyJson5Stuffs(content), JsonObject.class);
            }
        }
        current.add("%s.%s".formatted(configEntry.namespace, name), JsonUtils.parseObject(value));

        try (var writer = Files.newBufferedWriter(ProbePaths.SETTINGS_JSON)) {
            JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(writer);
            jsonWriter.setIndent("    ");
            ProbeJS.GSON_WRITER.toJson(current, JsonObject.class, jsonWriter);
        }
    }

    private static Object getConfigEntry(ConfigEntry<?> configEntry) throws IOException {
        JsonObject current = new JsonObject();
        if (Files.exists(ProbePaths.SETTINGS_JSON)) {
            try (var reader = Files.newBufferedReader(ProbePaths.SETTINGS_JSON)) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                current = ProbeJS.GSON.fromJson(JsonUtils.stripSussyJson5Stuffs(content), JsonObject.class);
            }
        }
        if (JsonUtils.deserializeObject(current) instanceof Map<?, ?> map) {
            return map.get("%s.%s".formatted(configEntry.namespace, configEntry.name));
        }
        return null;
    }

}
