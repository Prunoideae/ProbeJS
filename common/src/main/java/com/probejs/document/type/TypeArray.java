package com.probejs.document.type;

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

}
