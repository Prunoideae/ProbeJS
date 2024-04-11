package com.probejs.next.java.type.impl;

import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.java.type.TypeAdapter;
import com.probejs.next.java.type.TypeDescriptor;

import java.lang.reflect.AnnotatedArrayType;
import java.util.Collection;
import java.util.stream.Stream;

public class ArrayType extends TypeDescriptor {
    private final TypeDescriptor component;

    public ArrayType(AnnotatedArrayType arrayType) {
        super(arrayType.getAnnotations());
        this.component = TypeAdapter.getTypeDescription(arrayType.getAnnotatedGenericComponentType());
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return component.stream();
    }

    @Override
    public Collection<ClassPath> getPackages() {
        return component.getPackages();
    }

    @Override
    public int hashCode() {
        return component.hashCode() * 31;
    }
}
