package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocumentConstructor extends AbstractDocument<DocumentConstructor> {
    private String name;
    private final List<PropertyParam> params = new ArrayList<>();

    public DocumentConstructor() {

    }

    public DocumentConstructor(String name, List<PropertyParam> params) {
        this.name = name;
        this.params.addAll(params);
    }


    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("name", name);
        Serde.serializeCollection(object, "params", params);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        name = object.get("name").getAsString();
        Serde.deserializeDocuments(params, object.get("params"));
    }

    @Override
    public DocumentConstructor copy() {
        return new DocumentConstructor(name, params);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentConstructor that = (DocumentConstructor) o;
        return Objects.equals(name, that.name) && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, params);
    }
}
