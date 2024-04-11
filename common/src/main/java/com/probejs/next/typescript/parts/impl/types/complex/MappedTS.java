package com.probejs.next.typescript.parts.impl.types.complex;

import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.typescript.Reference;
import com.probejs.next.typescript.parts.impl.types.TypeTS;

import java.util.Map;

/**
 * Similar to ObjectTS, but supports mapping of key type to value type.
 * <br>
 * e.g. { [key: string]: number }
 */
public class MappedTS extends TypeTS {
    private final TypeTS key;
    private final TypeTS value;

    /**
     * The type of the value. true will make the object formatted as
     * <br>
     * { [key in K] : V }
     */
    private final boolean isLiteral;

    public MappedTS(TypeTS key, TypeTS value) {
        this(key, value, false);
    }

    public MappedTS(TypeTS key, TypeTS value, boolean isLiteral) {
        this.key = key;
        this.isLiteral = isLiteral;
        this.value = value;
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        key.setSymbols(symbols);
        value.setSymbols(symbols);
    }

    @Override
    public String getType() {
        return (isLiteral ? "{ [key in %s]: %s}" : "{ [key: %s]: %s}").formatted(key.getType(), value.getType());
    }
}
