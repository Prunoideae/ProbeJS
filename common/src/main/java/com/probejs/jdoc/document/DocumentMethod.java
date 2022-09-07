package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.info.MethodInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.jdoc.property.PropertyType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocumentMethod extends AbstractDocument<DocumentMethod> {
    private String name;
    private boolean isStatic;
    private PropertyType<?> returns;
    private final List<PropertyParam> params = new ArrayList<>();


    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("name", name);
        object.addProperty("static", isStatic);
        Serde.serializeCollection(object, "params", params);
        object.add("returns", returns.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        name = object.get("name").getAsString();
        isStatic = object.get("static").getAsBoolean();
        Serde.deserializeDocuments(params, object.get("params"));
        returns = (PropertyType<?>) Serde.deserializeProperty(object.get("returns").getAsJsonObject());
    }

    public static DocumentMethod fromJava(MethodInfo info) {
        DocumentMethod document = new DocumentMethod();
        document.name = info.getName();
        document.returns = Serde.deserializeFromJavaType(info.getReturnType());
        if (info.isNonnull())
            document.returns = PropertyType.wrapNonNull(document.returns);
        document.isStatic = info.isStatic();
        info.getParams().stream()
                .map(PropertyParam::fromJava)
                .forEach(document.params::add);
        return document;
    }

    @Override
    public DocumentMethod copy() {
        DocumentMethod document = new DocumentMethod();
        document.name = name;
        document.params.addAll(params);
        document.returns = returns;
        document.properties.addAll(properties);
        return document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentMethod that = (DocumentMethod) o;
        return Objects.equals(name, that.name) && Objects.equals(params, that.params) && Objects.equals(returns, that.returns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, params, returns);
    }

    public String getName() {
        return name;
    }

    public List<PropertyParam> getParams() {
        return params;
    }

    public PropertyType<?> getReturns() {
        return returns;
    }

    public boolean isStatic() {
        return isStatic;
    }
}
