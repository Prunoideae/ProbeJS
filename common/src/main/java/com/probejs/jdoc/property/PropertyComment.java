package com.probejs.jdoc.property;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class PropertyComment extends AbstractProperty<PropertyComment> {
    private final List<String> lines = new ArrayList<>();

    public PropertyComment() {
    }

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
        List<String> formatted = new ArrayList<>();
        formatted.add(" ".repeat(indent) + "/**");
        lines.forEach(line -> formatted.add(" ".repeat(indent) + " * "));
        formatted.add(" ".repeat(indent) + "*/");
        return formatted;
    }

    @Override
    public PropertyComment copy() {
        PropertyComment comment = new PropertyComment();
        comment.lines.addAll(lines);
        return comment;
    }

    @Override
    public PropertyComment merge(PropertyComment other) {
        PropertyComment comment = copy();
        comment.lines.add("");
        comment.lines.addAll(other.lines);
        return comment;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
