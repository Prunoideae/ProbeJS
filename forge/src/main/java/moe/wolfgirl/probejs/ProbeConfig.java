package moe.wolfgirl.probejs;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import moe.wolfgirl.probejs.utils.JsonUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Read / write to .vscode/settings.json
 */
public class ProbeConfig {
    public static ProbeConfig INSTANCE = new ProbeConfig();

    public ConfigEntry<Boolean> enabled = new ConfigEntry<>("enabled", true);
    public ConfigEntry<Boolean> enableDecompiler = new ConfigEntry<>("enableDecompiler", false);
    public ConfigEntry<Boolean> aggressive = new ConfigEntry<>("aggressive", false);
    public ConfigEntry<Boolean> interactive = new ConfigEntry<>("interactive", false);
    public ConfigEntry<Integer> interactivePort = new ConfigEntry<>("interactivePort", 7796);
    public ConfigEntry<Long> modHash = new ConfigEntry<>("modHash", -1L);
    public ConfigEntry<Long> registryHash = new ConfigEntry<>("registryHash", -1L);
    public ConfigEntry<Boolean> isolatedScopes = new ConfigEntry<>("isolatedScope", true);

    public static class ConfigEntry<T> {
        public final String name;
        public final T defaultValue;
        private T value;


        ConfigEntry(String name, @Nonnull T defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public void set(T value) {
            if (value == null) value = defaultValue;
            if (Objects.equals(this.value, value)) return;
            this.value = value;
            try {
                writeConfigEntry(name, this.value);
            } catch (IOException ignored) {
            }
        }

        public T get() {
            try {
                fromSetting();
            } catch (IOException e) {
                return defaultValue;
            }
            return value == null ? defaultValue : value;
        }

        @SuppressWarnings("unchecked")
        private void fromSetting() throws IOException {
            Class<?> typeClass = defaultValue.getClass();
            Object configValue = getConfigEntry(name);

            if (configValue == null) value = null;
            else if (configValue instanceof Number number) {
                if (typeClass == Integer.class) configValue = number.intValue();
                if (typeClass == Float.class) configValue = number.floatValue();
                if (typeClass == Long.class) configValue = number.longValue();
                if (typeClass == Byte.class) configValue = number.byteValue();
                if (typeClass == Double.class) configValue = number.doubleValue();
                if (typeClass == Short.class) configValue = number.shortValue();
                value = (T) configValue;
            } else if (typeClass.isInstance(configValue)) {
                value = (T) configValue;
            }
        }
    }

    private static void writeConfigEntry(String name, Object value) throws IOException {
        JsonObject current = new JsonObject();
        if (Files.exists(ProbePaths.SETTINGS_JSON)) {
            try (var reader = Files.newBufferedReader(ProbePaths.SETTINGS_JSON)) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                current = ProbeJS.GSON.fromJson(JsonUtils.stripSussyJson5Stuffs(content), JsonObject.class);
            }
        }
        current.add("probejs.%s".formatted(name), JsonUtils.parseObject(value));

        try (var writer = Files.newBufferedWriter(ProbePaths.SETTINGS_JSON)) {
            JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(writer);
            jsonWriter.setIndent("    ");
            ProbeJS.GSON_WRITER.toJson(current, JsonObject.class, jsonWriter);
        }
    }

    private static Object getConfigEntry(String name) throws IOException {
        JsonObject current = new JsonObject();
        if (Files.exists(ProbePaths.SETTINGS_JSON)) {
            try (var reader = Files.newBufferedReader(ProbePaths.SETTINGS_JSON)) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                current = ProbeJS.GSON.fromJson(JsonUtils.stripSussyJson5Stuffs(content), JsonObject.class);
            }
        }
        if (JsonUtils.deserializeObject(current) instanceof Map<?, ?> map) {
            return map.get("probejs.%s".formatted(name));
        }
        return null;
    }
}
