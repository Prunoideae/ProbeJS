package com.probejs.jdoc.property;

import com.google.gson.JsonObject;

public class PropertyHide extends AbstractProperty<PropertyHide> {
    @Override
    public void deserialize(JsonObject object) {

    }

    @Override
    public PropertyHide copy() {
        return new PropertyHide();
    }
}
