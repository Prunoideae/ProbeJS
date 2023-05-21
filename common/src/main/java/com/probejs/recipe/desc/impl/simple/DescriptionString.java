package com.probejs.recipe.desc.impl.simple;

import com.google.gson.JsonObject;
import com.probejs.recipe.desc.DescriptionTyped;

public class DescriptionString extends DescriptionTyped<String> {
    @Override
    public void deserialize(JsonObject json) {

    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}
