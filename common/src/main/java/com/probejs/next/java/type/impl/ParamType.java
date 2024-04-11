package com.probejs.next.java.type.impl;

import com.probejs.next.java.type.TypeAdapter;
import com.probejs.next.java.type.TypeDescriptor;

import java.lang.reflect.AnnotatedParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParamType extends TypeDescriptor {
    private final TypeDescriptor base;
    private final List<TypeDescriptor> params;

    public ParamType(AnnotatedParameterizedType annotatedType) {
        super(annotatedType.getAnnotations());
        this.base = TypeAdapter.getTypeDescription(annotatedType.getAnnotatedOwnerType());
        this.params = Arrays.stream(annotatedType.getAnnotatedActualTypeArguments()).map(TypeAdapter::getTypeDescription).collect(Collectors.toList());

    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return Stream.concat(base.stream(), params.stream().flatMap(TypeDescriptor::stream));
    }

    @Override
    public int hashCode() {
        return base.hashCode() * 31 + params.hashCode();
    }

    public TypeDescriptor getBase() {
        return base;
    }

    public List<TypeDescriptor> getParams() {
        return params;
    }
}
