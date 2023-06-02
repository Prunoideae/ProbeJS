package com.probejs.recipe.desc.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.ProbeJS;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;

import java.util.List;
import java.util.stream.Stream;

public class DescriptionNumber extends Description {
    String numberType = "int";

    @Override
    public void deserialize(JsonObject json) {
        numberType = json.get("number_type").getAsString();
    }

    @Override
    public PropertyType<?> describeType() {
        return new PropertyType.Native("number");
    }

    @Override
    public String generateParamDoc() {
        return numberType;
    }

    @Override
    public IFormatter transformDefaultValue(JsonElement defaultValue) {
        if (!defaultValue.isJsonPrimitive())
            return null;
        var primitive = defaultValue.getAsJsonPrimitive();
        if (!primitive.isNumber())
            return null;
        return (i, s) -> List.of(ProbeJS.GSON.toJson(primitive.getAsNumber()));
    }
}
