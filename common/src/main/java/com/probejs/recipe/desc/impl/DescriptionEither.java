package com.probejs.recipe.desc.impl;

import com.google.gson.JsonObject;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;

import java.util.ArrayList;
import java.util.List;

public class DescriptionEither extends Description {
    private final List<Description> components = new ArrayList<>();

    @Override
    public void deserialize(JsonObject json) {
        //TODO: see lat's implementation
    }

    @Override
    public PropertyType<?> describeType() {
        List<PropertyType<?>> componentTypes = new ArrayList<>();
        for (Description component : components) {
            componentTypes.add(component.describeType());
        }
        return new PropertyType.Union(componentTypes);
    }
}
