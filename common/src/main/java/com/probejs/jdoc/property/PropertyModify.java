package com.probejs.jdoc.property;

import com.google.gson.JsonObject;
import com.probejs.jdoc.Serde;

public class PropertyModify extends AbstractProperty {
    private String name;
    private PropertyType newType;

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("name", name);
        object.add("type", newType.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        name = object.get("name").getAsString();
        newType = (PropertyType) Serde.deserializeProperty(object.get("type").getAsJsonObject());
    }
}
