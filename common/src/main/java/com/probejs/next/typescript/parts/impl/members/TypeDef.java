package com.probejs.next.typescript.parts.impl.members;

import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.typescript.Reference;
import com.probejs.next.typescript.parts.Code;
import com.probejs.next.typescript.parts.impl.types.TypeTS;

import java.util.List;
import java.util.Map;

/**
 * type Something = ...
 */
public class TypeDef extends Code {

    private final String name;
    private final TypeTS type;

    public TypeDef(String name, TypeTS type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        type.setSymbols(symbols);
    }

    @Override
    public List<String> getContent() {
        return List.of("type %s = %s;".formatted(name, type.getType()));
    }
}
