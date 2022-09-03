package com.probejs.jdoc.property;

import com.google.gson.JsonObject;

public class PropertyRename extends AbstractProperty {
    protected String old;
    protected String newName;

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("old", old);
        object.addProperty("new", newName);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        old = object.get("old").getAsString();
        newName = object.get("new").getAsString();
    }
}
