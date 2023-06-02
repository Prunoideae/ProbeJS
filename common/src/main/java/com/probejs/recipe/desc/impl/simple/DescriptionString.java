package com.probejs.recipe.desc.impl.simple;

import com.google.gson.JsonElement;
import com.probejs.ProbeJS;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.recipe.desc.SimpleDescription;

import java.util.List;
import java.util.stream.Stream;

public class DescriptionString extends SimpleDescription<String> {
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
