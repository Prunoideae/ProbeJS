package com.probejs.features.extension;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class VSCodeManager {
    public static JsonElement onFilesChanged(JsonObject payload) {
        return JsonNull.INSTANCE;
    }
}
