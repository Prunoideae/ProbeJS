package com.probejs.next.typescript.parts.impl.types;

/**
 * A "Native" type means something that already exists in TypeScript.
 * <br>
 * e.g. InstanceType, ReturnType, etc.
 * <br>
 * Should note that primitive types like string or number are generated
 * via symbol table.
 */
public class NativeTS extends TypeTS {

    private final String type;

    public NativeTS(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
}
