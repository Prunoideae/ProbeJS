package com.probejs.recipe.desc.impl.simple;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;
import com.probejs.recipe.desc.DescriptionTyped;

import java.util.List;

public class DescriptionBoolean extends DescriptionTyped<Boolean> {
    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public void deserialize(JsonObject json) {

    }

    @Override
    public IFormatter transformDefaultValue(JsonElement defaultValue) {
        if (!defaultValue.isJsonPrimitive())
            return null;
        var primitive = defaultValue.getAsJsonPrimitive();
        if (!primitive.isBoolean())
            return null;
        return (i, s) -> List.of(primitive.getAsBoolean() ? "true" : "false");
    }
}
