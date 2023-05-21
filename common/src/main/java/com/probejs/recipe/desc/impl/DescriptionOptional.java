package com.probejs.recipe.desc.impl;

import com.google.gson.JsonObject;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;
import com.probejs.recipe.desc.DescriptionRegistry;

/**
 * Transparently wraps another description. Used as a marker for optional parameters.
 */
public class DescriptionOptional extends Description {
    private Description component = null;

    @Override
    public void deserialize(JsonObject json) {
        component = DescriptionRegistry.getDescription(json.get("component").getAsJsonObject());
    }

    @Override
    public PropertyType<?> describeType() {
        return component.describeType();
    }
}
