package moe.wolfgirl.probejs.lang.java.type.impl;

import moe.wolfgirl.probejs.lang.java.type.TypeAdapter;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.Optional;
import java.util.stream.Stream;

public class WildType extends TypeDescriptor {
    public Optional<TypeDescriptor> bound;

    public WildType(AnnotatedWildcardType wildcardType) {
        super(wildcardType.getAnnotations());
        if (wildcardType.getAnnotatedLowerBounds().length != 0) {
            bound = Optional.of(TypeAdapter.getTypeDescription(wildcardType.getAnnotatedLowerBounds()[0]));
        } else if (!wildcardType.getAnnotatedUpperBounds()[0].getType().equals(Object.class)) {
            bound = Optional.of(TypeAdapter.getTypeDescription(wildcardType.getAnnotatedUpperBounds()[0]));
        } else {
            bound = Optional.empty();
        }
    }

    public WildType(java.lang.reflect.WildcardType wildcardType) {
        super(new Annotation[]{});
        if (wildcardType.getLowerBounds().length != 0) {
            bound = Optional.of(TypeAdapter.getTypeDescription(wildcardType.getLowerBounds()[0]));
        } else if (!wildcardType.getUpperBounds()[0].equals(Object.class)) {
            bound = Optional.of(TypeAdapter.getTypeDescription(wildcardType.getUpperBounds()[0]));
        } else {
            bound = Optional.empty();
        }
    }

    public WildType(Optional<TypeDescriptor> bound) {
        super(new Annotation[]{});
        this.bound = bound;
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return bound.stream();
    }
}
