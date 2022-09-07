package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocumentConstructor extends AbstractDocument<DocumentConstructor> {
    private final List<PropertyParam> params = new ArrayList<>();

    public DocumentConstructor() {

    }

    public DocumentConstructor(List<PropertyParam> params) {
        this.params.addAll(params);
    }


    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        Serde.serializeCollection(object, "params", params);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        Serde.deserializeDocuments(params, object.get("params"));
    }

    @Override
    public DocumentConstructor copy() {
        return new DocumentConstructor(params);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentConstructor that = (DocumentConstructor) o;
        return Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params);
    }

    public List<PropertyParam> getParams() {
        return params;
    }
}
