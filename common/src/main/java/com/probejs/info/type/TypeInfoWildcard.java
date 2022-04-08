package com.probejs.info.type;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public class TypeInfoWildcard implements ITypeInfo {


    public static boolean test(Type type) {
        return type instanceof WildcardType;
    }

    private final ITypeInfo type;

    public TypeInfoWildcard(Type type) {
        if (type instanceof WildcardType wild) {
            Type[] upper = wild.getUpperBounds();
            Type[] lower = wild.getLowerBounds();
            if (upper[0] != Object.class) {
                this.type = InfoTypeResolver.resolveType(upper[0]);
                return;
            }
            if (lower.length != 0) {
                this.type = InfoTypeResolver.resolveType(lower[0]);
                return;
            }
        }
        this.type = new TypeInfoClass(Object.class);
    }

    private TypeInfoWildcard(ITypeInfo inner) {
        this.type = inner;
    }

    @Override
    public ITypeInfo getBaseType() {
        return type;
    }

    @Override
    public Class<?> getResolvedClass() {
        return type.getResolvedClass();
    }

    @Override
    public String getTypeName() {
        return type.getTypeName();
    }

    @Override
    public ITypeInfo copy() {
        return new TypeInfoWildcard(type.copy());
    }

    @Override
    public boolean assignableFrom(ITypeInfo info) {
        return info.getBaseType().assignableFrom(type);
    }
}
