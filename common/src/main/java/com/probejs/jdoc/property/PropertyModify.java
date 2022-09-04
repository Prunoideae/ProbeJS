package com.probejs.jdoc.property;

import com.google.gson.JsonObject;
import com.probejs.jdoc.Serde;

public class PropertyModify extends AbstractProperty<PropertyModify> {
    private int ordinal;
    private PropertyType<?> newType;

    public PropertyModify() {
    }

    public PropertyModify(int ordinal, PropertyType<?> newType) {
        this.ordinal = ordinal;
        this.newType = newType;
    }


    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("index", ordinal);
        object.add("type", newType.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        ordinal = object.get("index").getAsInt();
        newType = (PropertyType<?>) Serde.deserializeProperty(object.get("type").getAsJsonObject());
    }

    @Override
    public PropertyModify copy() {
        return new PropertyModify(ordinal, newType);
    }
}
