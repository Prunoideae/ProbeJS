package com.probejs.jdoc.property;

import com.google.gson.JsonObject;

public class PropertyUnderscored extends AbstractProperty<PropertyUnderscored> {
    private boolean underscored = true;

    @Override
    public void deserialize(JsonObject object) {
        if (object.has("underscored"))
            underscored = object.get("underscored").getAsBoolean();
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("underscored", underscored);
        return object;
    }

    @Override
    public PropertyUnderscored copy() {
        return new PropertyUnderscored();
    }

    public boolean isUnderscored() {
        return underscored;
    }
}
