package com.probejs.jdoc.java.type;

import com.probejs.jdoc.java.MethodInfo;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeInfoClass implements ITypeInfo {
    public static boolean test(Type type) {
        return type instanceof Class<?>;
    }

    private final Class<?> type;

    public TypeInfoClass(Type type, Function<Type, Type> typeTransformer) {
        this.type = (Class<?>) type;
    }

    public TypeInfoClass(Class<?> type) {
        this.type = type;
    }

    @Override
    public ITypeInfo getBaseType() {
        return this;
    }

    @Override
    public Class<?> getResolvedClass() {
        return type;
    }

    @Override
    public String getTypeName() {
        return MethodInfo.getRemappedOrOriginalClass(type);
    }

    @Override
    public ITypeInfo copy() {
        return new TypeInfoClass(type);
    }

    @Override
    public boolean assignableFrom(ITypeInfo info) {
        return info instanceof TypeInfoClass clazz && clazz.type.isAssignableFrom(type);
    }

    @Override
    public boolean equalsTo(ITypeInfo info) {
        return info instanceof TypeInfoClass clazz && clazz.type.equals(type);
    }

    public List<ITypeInfo> getTypeVariables() {
        return Arrays.stream(type.getTypeParameters()).map(InfoTypeResolver::resolveType).collect(Collectors.toList());
    }
}
