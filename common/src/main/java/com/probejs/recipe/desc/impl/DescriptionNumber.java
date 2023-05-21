package com.probejs.recipe.desc.impl;

import com.google.gson.JsonObject;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;

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
}
