package com.probejs.next.typescript.parts.impl.types;

/**
 * A primitive TS type means it is a JSON Primitive literal
 * <br>
 * e.g. "abc", 42, 123.456, false
 */
public class PrimitiveTS extends TypeTS {
    private final Object inner;

    public PrimitiveTS(Number number) {
        this.inner = number;
    }

    @Override
    public String getType() {
        return null;
    }
}
