package com.probejs.jdoc.java.type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

public class TypeInfoArray implements ITypeInfo {
    public static boolean test1(Type type) {
        return type instanceof GenericArrayType;
    }

    public static boolean test2(Type type) {
        return type instanceof Class<?> clazz && clazz.isArray();
    }

    public static boolean test(Type type) {
        return test1(type) || test2(type);
    }

    private ITypeInfo type;

    public TypeInfoArray(Type type, Function<Type, Type> typeTransformer) {
        if (test1(type))
            this.type = InfoTypeResolver.resolveType(((GenericArrayType) type).getGenericComponentType(), typeTransformer);
        if (test2(type)) {
            assert type instanceof Class<?>;
            this.type = InfoTypeResolver.resolveType(((Class<?>) type).getComponentType(), typeTransformer);
        }
    }

    private TypeInfoArray(ITypeInfo inner) {
        this.type = inner;
    }

    @Override
    public ITypeInfo getBaseType() {
        return type;
    }

    @Override
    public Class<?> getResolvedClass() {
        return List.class;
    }

    @Override
    public String getTypeName() {
        return type.getTypeName() + "[]";
    }


    public void setType(ITypeInfo type) {
        this.type = type;
    }

    @Override
    public ITypeInfo copy() {
        return new TypeInfoArray(type.copy());
    }

    @Override
    public boolean assignableFrom(ITypeInfo info) {
        return info instanceof TypeInfoArray && info.getBaseType().assignableFrom(type);
    }
}
