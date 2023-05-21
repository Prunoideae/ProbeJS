package com.probejs.recipe.desc.impl;

import com.google.gson.JsonObject;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;
import com.probejs.recipe.desc.DescriptionRegistry;

public class DescriptionPatternKey extends Description {
    private Description component = null;

    @Override
    public void deserialize(JsonObject json) {
        component = DescriptionRegistry.getDescription(json.get("component").getAsJsonObject());
    }

    @Override
    public PropertyType<?> describeType() {
        return new PropertyType.JSObject()
                .add(new PropertyType.JSObjectKey().withType(new PropertyType.Native("string")),
                        component.describeType());
    }
}
