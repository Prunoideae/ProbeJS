package com.probejs.next.typescript.parts.impl.types;

import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.typescript.Reference;

import java.util.List;
import java.util.Map;

public final class ArrayTS extends TypeTS {
    private final TypeTS component;

    public ArrayTS(TypeTS component) {
        this.component = component;
    }


    @Override
    public String getType() {
        return getMaybeWrapped(component) + "[]";
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        component.setSymbols(symbols);
    }
}
