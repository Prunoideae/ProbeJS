package com.probejs.info.type;

public interface ITypeInfo {

    ITypeInfo getBaseType();

    Class<?> getResolvedClass();

    String getTypeName();

    ITypeInfo copy();

    boolean assignableFrom(ITypeInfo info);
}
