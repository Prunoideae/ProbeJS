package com.probejs.next.typescript.parts.impl.types;

import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.typescript.Reference;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TypeVariableTS extends TypeTS {

    private final String symbol;
    private final TypeTS bounds;

    public TypeVariableTS(String symbol, @Nullable TypeTS bounds) {
        this.symbol = symbol;
        this.bounds = bounds;
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        if (bounds != null) bounds.setSymbols(symbols);
    }

    @Override
    public String getType() {
        return bounds == null ? symbol : "%s extends %s".formatted(symbol, bounds.getType());
    }
}
