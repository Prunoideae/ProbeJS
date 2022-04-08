package com.probejs.info.type;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class TypeInfoVariable implements ITypeInfo {
    public static boolean test(Type type) {
        return type instanceof TypeVariable;
    }

    private final TypeVariable<?> type;
    private boolean underscored = false;

    public TypeInfoVariable(Type type) {
        this.type = (TypeVariable<?>) type;
    }

    private TypeInfoVariable(TypeVariable<?> inner) {
        this.type = inner;
    }

    public void setUnderscored(boolean underscored) {
        this.underscored = underscored;
    }

    @Override
    public ITypeInfo getBaseType() {
        return this;
    }

    @Override
    public String getTypeName() {
        return type.getName() + (underscored ? "_" : "");
    }

    @Override
    public ITypeInfo copy() {
        TypeInfoVariable copied = new TypeInfoVariable(type);
        copied.setUnderscored(underscored);
        return copied;
    }

    @Override
    public boolean assignableFrom(ITypeInfo info) {
        return info instanceof TypeInfoVariable;
    }

    @Override
    public Class<?> getResolvedClass() {
        return Object.class;
    }
}
