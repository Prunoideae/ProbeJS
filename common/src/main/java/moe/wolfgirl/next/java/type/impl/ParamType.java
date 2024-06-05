package moe.wolfgirl.next.java.type.impl;

import moe.wolfgirl.next.java.type.TypeAdapter;
import moe.wolfgirl.next.java.type.TypeDescriptor;

import java.lang.reflect.AnnotatedParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParamType extends TypeDescriptor {
    public final TypeDescriptor base;
    public final List<TypeDescriptor> params;

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
}
