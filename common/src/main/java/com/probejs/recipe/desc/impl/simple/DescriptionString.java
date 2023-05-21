package com.probejs.recipe.desc.impl.simple;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.ProbeJS;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.recipe.desc.DescriptionTyped;

import java.util.List;

public class DescriptionString extends DescriptionTyped<String> {
    @Override
    public void deserialize(JsonObject json) {

    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public IFormatter transformDefaultValue(JsonElement defaultValue) {
        if (!defaultValue.isJsonPrimitive())
            return null;
        var primitive = defaultValue.getAsJsonPrimitive();
        if (!primitive.isString())
            return null;
        return (i, s) -> List.of(ProbeJS.GSON.toJson(primitive.getAsString()));
    }
}
