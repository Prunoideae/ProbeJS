package com.probejs.recipe.desc.impl;

import com.google.gson.JsonObject;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.compiler.formatter.formatter.jdoc.FormatterValue;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;
import com.probejs.recipe.desc.DescriptionRegistry;

/**
 * Transparently wraps another description. Used as a marker for optional parameters.
 */
public class DescriptionOptional extends Description {
    private Description component = null;
    private IFormatter defaultValue = null;

    @Override
    public void deserialize(JsonObject json) {
        component = DescriptionRegistry.getDescription(json.get("component").getAsJsonObject());
        if (json.has("default_value")) {
            IFormatter d = component.transformDefaultValue(json.get("default_value"));
            if (d != null) {
                defaultValue = d;
            }
        }
    }

    @Override
    public PropertyType<?> describeType() {
        return component.describeType();
    }

    public IFormatter getDefaultValueFormatter() {
        return defaultValue;
    }
}
