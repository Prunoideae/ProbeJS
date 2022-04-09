package com.probejs.document.type;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TypeArray implements IType {
    private final IType component;

    public TypeArray(IType component) {
        this.component = component;
    }

    public IType getComponent() {
        return component;
    }

    @Override
    public String getTypeName() {
        return component.getTypeName() + "[]";
    }

    @Override
    public String getTransformedName(BiFunction<IType, String, String> transformer) {
        return transformer.apply(this, component.getTransformedName(transformer) + "[]");
    }
}
