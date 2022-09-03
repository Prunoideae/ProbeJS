package com.probejs.jdoc.property;

import com.google.gson.JsonObject;

public class PropertyParam extends AbstractProperty {
    private String name;
    private PropertyType type;

    @Override
    public JsonObject serialize() {
        return super.serialize();
    }

    @Override
    public void deserialize(JsonObject object) {

    }
}
