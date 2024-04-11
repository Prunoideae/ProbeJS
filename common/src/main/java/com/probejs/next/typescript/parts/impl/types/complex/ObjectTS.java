package com.probejs.next.typescript.parts.impl.types.complex;

import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.typescript.Reference;
import com.probejs.next.typescript.parts.impl.types.TypeTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a TypeScript object type. Mapped type is not here.
 * <br>
 * e.g. { a: string, b: number }, not { [K in keyof T]: T[K] }
 */
public class ObjectTS extends TypeTS {

    private final Map<String, TypeTS> fields;

    public ObjectTS(Map<String, TypeTS> fields) {
        this.fields = fields;
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        for (TypeTS field : fields.values()) {
            field.setSymbols(symbols);
        }
    }

    @Override
    public String getType() {
        List<String> fields = new ArrayList<>();
        for (Map.Entry<String, TypeTS> entry : this.fields.entrySet()) {
            fields.add("%s: %s".formatted(entry.getKey(), entry.getValue().getType()));
        }
        return "{%s}".formatted(String.join(", ", fields));
    }
}
