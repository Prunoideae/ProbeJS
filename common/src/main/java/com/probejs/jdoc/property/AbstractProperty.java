package com.probejs.jdoc.property;

import com.google.gson.JsonObject;
import com.probejs.jdoc.document.AbstractDocument;

/**
 * Represents a property.
 * <p>
 * You must ensure that the parameter-less constructor <b>does not</b> depend
 * on any other stateful objects. As this property might be constructed at any time.
 */
public abstract class AbstractProperty<T extends AbstractDocument<T>> extends AbstractDocument<T> {
    @Override
    @SuppressWarnings("unchecked")
    public T applyProperties() {
        return (T) this;
    }

    public abstract void deserialize(JsonObject object);
}
