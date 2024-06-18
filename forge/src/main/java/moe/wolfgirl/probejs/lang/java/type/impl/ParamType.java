package moe.wolfgirl.probejs.lang.java.type.impl;

import moe.wolfgirl.probejs.lang.java.type.TypeAdapter;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParamType extends TypeDescriptor {
    public TypeDescriptor base;
    public final List<TypeDescriptor> params;

    public ParamType(AnnotatedParameterizedType annotatedType) {
        super(annotatedType.getAnnotations());
        this.base = TypeAdapter.getTypeDescription(((ParameterizedType) annotatedType.getType()).getRawType(), false);
        this.params = Arrays.stream(annotatedType.getAnnotatedActualTypeArguments()).map(t -> TypeAdapter.getTypeDescription(t, false)).collect(Collectors.toList());
    }

    public ParamType(ParameterizedType parameterizedType) {
        super(new Annotation[]{});
        this.base = TypeAdapter.getTypeDescription(parameterizedType.getRawType(), false);
        this.params = Arrays.stream(parameterizedType.getActualTypeArguments()).map(t -> TypeAdapter.getTypeDescription(t, false)).collect(Collectors.toList());
    }

    public ParamType(Annotation[] annotations, TypeDescriptor base, List<TypeDescriptor> params) {
        super(annotations);
        this.base = base;
        this.params = params;
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
