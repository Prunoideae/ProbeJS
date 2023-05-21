package com.probejs.recipe.desc.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public IFormatter transformDefaultValue(JsonElement defaultValue) {
        return components.stream()
                .map(component -> component.transformDefaultValue(defaultValue))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
