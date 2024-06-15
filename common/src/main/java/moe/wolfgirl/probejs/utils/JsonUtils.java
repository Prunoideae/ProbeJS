package moe.wolfgirl.probejs.utils;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class JsonUtils {
    private static final Pattern MATCH_TRAILING = Pattern.compile(",(?!\\s*?[{\\[\"'\\w])");

    public static String stripSussyJson5Stuffs(String jsonc) {
        StringBuilder sb = new StringBuilder();

        // remove comments
        for (String line : (Iterable<String>) jsonc.lines()::iterator) {
            // Split by // first
            String[] parts = line.strip().split("//");

            // first string is always included
            boolean enclosed = false;
            int quotes = 0;
            int escaped = 0;
            for (int i = 0; i < parts.length; i++) {
                var part = parts[i];
                // If the previous quotes are enclosed, then // should be a comment
                if (enclosed) break;

                // If it's not enclosed but not at the start, then // is in a string
                if (i != 0) sb.append("//");
                sb.append(part);

                quotes += StringUtils.countMatches(part, "\"");
                escaped += StringUtils.countMatches(part, "\\\"");
                // Test if the quotes are enclosed
                enclosed = (quotes - escaped) % 2 == 0;
            }
        }

        // remove trailing comma
        return MATCH_TRAILING.matcher(sb.toString()).replaceAll("").strip();
    }

    public static JsonArray asStringArray(Collection<String> array) {
        JsonArray jsonArray = new JsonArray();
        for (String s : array) {
            jsonArray.add(s);
        }
        return jsonArray;
    }

    public static JsonElement parseObject(Object obj) {
        if (obj instanceof Number number) {
            return new JsonPrimitive(number);
        } else if (obj instanceof String string) {
            return new JsonPrimitive(string);
        } else if (obj instanceof Boolean bool) {
            return new JsonPrimitive(bool);
        } else if (obj instanceof List<?> list) {
            JsonArray jsonArray = new JsonArray(list.size());
            for (Object o : list) {
                jsonArray.add(parseObject(o));
            }
            return jsonArray;
        } else if (obj instanceof Map<?, ?> map) {
            JsonObject object = new JsonObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object k = entry.getKey();
                Object v = entry.getValue();
                if (k instanceof String s) {
                    object.add(s, parseObject(v));
                }
            }
            return object;
        }
        return JsonNull.INSTANCE;
    }

    public static Object deserializeObject(JsonElement jsonElement) {
        if (jsonElement instanceof JsonPrimitive primitive) {
            if (primitive.isBoolean()) return primitive.getAsBoolean();
            if (primitive.isString()) return primitive.getAsString();
            if (primitive.isNumber()) return primitive.getAsNumber();
        } else if (jsonElement instanceof JsonArray array) {
            List<Object> deserialized = new ArrayList<>();
            for (JsonElement element : array) {
                deserialized.add(deserializeObject(element));
            }
            return deserialized;
        } else if (jsonElement instanceof JsonObject object) {
            Map<String, Object> deserialized = new HashMap<>();
            for (String s : object.keySet()) {
                deserialized.put(s, deserializeObject(object.get(s)));
            }
            return deserialized;
        }

        return null;
    }

    public static JsonElement mergeJsonRecursively(JsonElement first, JsonElement second) {
        if (first instanceof JsonObject firstObject && second instanceof JsonObject secondObject) {
            var result = firstObject.deepCopy();
            for (Map.Entry<String, JsonElement> entry : secondObject.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (result.has(key)) {
                    result.add(key, mergeJsonRecursively(result.get(key), value));
                } else {
                    result.add(key, value);
                }
            }
            return result;
        }

        if (first instanceof JsonArray firstArray && second instanceof JsonArray secondArray) {
            List<JsonElement> elements = new ArrayList<>();
            for (JsonElement element : firstArray) {
                elements.add(element.deepCopy());
            }
            for (JsonElement element : secondArray) {
                int index;
                if ((index = elements.indexOf(element)) != -1) {
                    elements.set(index, mergeJsonRecursively(elements.get(index), element));
                } else {
                    elements.add(element);
                }
            }
            JsonArray result = new JsonArray();
            for (JsonElement element : elements) {
                result.add(element);
            }
            return result;
        }

        return second;
    }
}
