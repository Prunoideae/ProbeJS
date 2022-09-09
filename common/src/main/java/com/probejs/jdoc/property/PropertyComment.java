package com.probejs.jdoc.property;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PropertyComment extends AbstractProperty<PropertyComment> {
    private final List<String> lines = new ArrayList<>();

    public PropertyComment() {
    }

    public PropertyComment(String... lines) {
        this.lines.addAll(List.of(lines));
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
        formatted.add(Util.indent(indent) + "/**");
        lines.forEach(line -> formatted.add(" ".repeat(indent) + " * "));
        formatted.add(Util.indent(indent) + "*/");
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
        if (this == other)
            return this;
        PropertyComment comment = copy();
        comment.lines.add("");
        comment.lines.addAll(other.lines);
        return comment;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyComment comment = (PropertyComment) o;
        return Objects.equals(lines, comment.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines);
    }
}
