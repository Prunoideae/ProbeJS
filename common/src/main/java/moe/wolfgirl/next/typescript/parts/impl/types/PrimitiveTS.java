package moe.wolfgirl.next.typescript.parts.impl.types;

import moe.wolfgirl.ProbeJS;

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

    public PrimitiveTS(String string) {
        this.inner = string;
    }

    public PrimitiveTS(Boolean bool) {
        this.inner = bool;
    }

    @Override
    public String getType() {
        return ProbeJS.GSON.toJson(inner);
    }
}
