package com.probejs.jdoc.property;

import com.google.gson.JsonObject;
import com.probejs.jdoc.Serde;

public class PropertyReturns extends AbstractProperty<PropertyReturns> {
    private PropertyType<?> type;

    public PropertyReturns() {
    }

    public PropertyReturns(PropertyType<?> type) {
        this.type = type;
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.add("type", type.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        type = (PropertyType<?>) Serde.deserializeProperty(object.get("type").getAsJsonObject());
    }

    @Override
    public PropertyReturns copy() {
        return new PropertyReturns(type);
    }
}
