package com.probejs.recipe.desc.impl.simple;

import com.google.gson.JsonObject;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;
import com.probejs.recipe.desc.DescriptionTyped;

public class DescriptionBoolean extends DescriptionTyped<Boolean> {
    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public void deserialize(JsonObject json) {

    }
}
