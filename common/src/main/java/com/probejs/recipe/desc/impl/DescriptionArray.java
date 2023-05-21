package com.probejs.recipe.desc.impl;

import com.google.gson.JsonObject;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;
import com.probejs.recipe.desc.DescriptionRegistry;

import java.util.List;

public class DescriptionArray extends Description {

    private Description component = null;
    private boolean canWriteSelf = true;

    @Override
    public void deserialize(JsonObject json) {
        component = DescriptionRegistry.getDescription(json.get("component").getAsJsonObject());
        canWriteSelf = json.get("can_write_self").getAsBoolean();
    }

    @Override
    public PropertyType<?> describeType() {
        if (component == null)
            return Description.ANY;
        PropertyType<?> componentType = component.describeType();
        PropertyType<?> arrayType = new PropertyType.Array(componentType);
        if (canWriteSelf) {
            return new PropertyType.Union(List.of(arrayType, componentType));
        } else {
            return arrayType;
        }
    }
}
