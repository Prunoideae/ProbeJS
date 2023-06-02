package com.probejs.recipe.desc.impl.simple;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.recipe.desc.SimpleDescription;

import java.util.List;

public class DescriptionBoolean extends SimpleDescription<Boolean> {
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
