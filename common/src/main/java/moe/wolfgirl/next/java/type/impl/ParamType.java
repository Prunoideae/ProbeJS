package moe.wolfgirl.next.java.type.impl;

import moe.wolfgirl.next.java.type.TypeAdapter;
import moe.wolfgirl.next.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParamType extends TypeDescriptor {
    public final TypeDescriptor base;
    public final List<TypeDescriptor> params;

    public ParamType(AnnotatedParameterizedType annotatedType) {
        super(annotatedType.getAnnotations());
        this.base = TypeAdapter.getTypeDescription(((ParameterizedType) annotatedType.getType()).getRawType());
        this.params = Arrays.stream(annotatedType.getAnnotatedActualTypeArguments()).map(t -> TypeAdapter.getTypeDescription(t, false)).collect(Collectors.toList());
    }

    public ParamType(ParameterizedType parameterizedType) {
        super(new Annotation[]{});
        this.base = TypeAdapter.getTypeDescription(parameterizedType.getRawType());
        this.params = Arrays.stream(parameterizedType.getActualTypeArguments()).map(t -> TypeAdapter.getTypeDescription(t, false)).collect(Collectors.toList());
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
