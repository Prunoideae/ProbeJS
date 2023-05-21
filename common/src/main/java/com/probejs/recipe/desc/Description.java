package com.probejs.recipe.desc;

import com.google.gson.JsonObject;
import com.probejs.jdoc.property.PropertyType;

import java.util.List;

public abstract class Description {
    public abstract void deserialize(JsonObject json);

    public abstract PropertyType<?> describeType();

    public String generateParamDoc() {
        return "";
    }

    public static final PropertyType.Native ANY = new PropertyType.Native("any");
    public static final Description FALLBACK = new Description() {
        @Override
        public void deserialize(JsonObject json) {
        }

        @Override
        public PropertyType<?> describeType() {
            return ANY;
        }
    };
}
