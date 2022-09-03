package com.probejs.jdoc.property;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class PropertyComment extends AbstractProperty {
    private final List<String> lines = new ArrayList<>();

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        JsonArray linesJson = new JsonArray();
        lines.forEach(linesJson::add);
        object.add("lines", linesJson);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        for (JsonElement element : object.get("lines").getAsJsonArray()) {
            lines.add(element.getAsString());
        }
    }

    public List<String> getLines() {
        return lines;
    }

    public List<String> formatLines(int indent) {
        return new ArrayList<>();
    }
}
