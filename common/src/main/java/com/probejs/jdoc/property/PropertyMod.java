package com.probejs.jdoc.property;

import com.google.gson.JsonObject;
import dev.architectury.platform.Platform;

public class PropertyMod extends AbstractProperty {

    private String mod;

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("mod", mod);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        mod = object.get("mod").getAsString();
    }

    public boolean isModLoaded() {
        return Platform.isModLoaded(mod);
    }
}
