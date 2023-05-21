package com.probejs.recipe.desc;

import com.google.gson.JsonObject;
import com.probejs.jdoc.property.PropertyType;

public abstract class DescriptionTyped<T> extends Description {

    public abstract Class<T> getType();

    @Override
    public void deserialize(JsonObject json) {

    }

    @Override
    public PropertyType<?> describeType() {
        return new PropertyType.Clazz(getType());
    }
}
