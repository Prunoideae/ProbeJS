package com.probejs.jdoc.property;

import com.probejs.jdoc.document.AbstractDocumentBase;

/**
 * Represents a property.
 * <p>
 * You must ensure that the parameter-less constructor <b>does not</b> depend
 * on any other stateful objects. As this property might be constructed at any time.
 */
public abstract class AbstractProperty<T extends AbstractDocumentBase<T>> extends AbstractDocumentBase<T> {
    @Override
    @SuppressWarnings("unchecked")
    public T applyProperties() {
        return (T) this;
    }

    //public abstract void deserialize(JsonObject object);
}
