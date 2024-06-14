package moe.wolfgirl.probejs.next.utils;

import com.google.gson.JsonArray;

import java.util.Collection;

public class JsonUtils {
    public static JsonArray asStringArray(Collection<String> array) {
        JsonArray jsonArray = new JsonArray();
        for (String s : array) {
            jsonArray.add(s);
        }
        return jsonArray;
    }
}
