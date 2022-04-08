package com.probejs.document.type;

import java.util.Set;
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
    public Set<String> getAssignableNames() {
        return component.getAssignableNames().stream().map(s -> s + "[]").collect(Collectors.toSet());
    }
}
