package com.probejs.jdoc.property;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.probejs.jdoc.ISerde;

/**
 * Represents a property.
 * <p>
 * You must ensure that the parameter-less constructor <b>does not</b> depend
 * on any other stateful objects. As this property might be constructed at any time.
 */
public abstract class AbstractProperty implements ISerde {
    public static final BiMap<Class<? extends AbstractProperty>, String> PROPERTY_TYPE_REGISTRY = HashBiMap.create();

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", PROPERTY_TYPE_REGISTRY.get(this.getClass()));
        return obj;
    }

    public abstract void deserialize(JsonObject object);
}
