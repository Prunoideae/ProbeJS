package moe.wolfgirl.probejs.lang.java.type.impl;

import com.mojang.datafixers.util.Either;
import moe.wolfgirl.probejs.lang.java.type.TypeAdapter;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.Optional;
import java.util.stream.Stream;

public class WildcardType extends TypeDescriptor {
    public Optional<Either<TypeDescriptor, TypeDescriptor>> bound;

    public WildcardType(AnnotatedWildcardType wildcardType, boolean checkBound) {
        super(wildcardType.getAnnotations());
        if (!checkBound) {
            bound = Optional.empty();
            return;
        }
        if (wildcardType.getAnnotatedLowerBounds().length != 0) {
            bound = Optional.of(Either.left(TypeAdapter.getTypeDescription(wildcardType.getAnnotatedLowerBounds()[0])));
        } else if (!wildcardType.getAnnotatedUpperBounds()[0].getType().equals(Object.class)) {
            bound = Optional.of(Either.right(TypeAdapter.getTypeDescription(wildcardType.getAnnotatedUpperBounds()[0])));
        } else {
            bound = Optional.empty();
        }
    }

    public WildcardType(java.lang.reflect.WildcardType wildcardType, boolean checkBound) {
        super(new Annotation[]{});
        if (!checkBound) {
            bound = Optional.empty();
            return;
        }
        if (wildcardType.getLowerBounds().length != 0) {
            bound = Optional.of(Either.left(TypeAdapter.getTypeDescription(wildcardType.getLowerBounds()[0])));
        } else if (!wildcardType.getUpperBounds()[0].equals(Object.class)) {
            bound = Optional.of(Either.right(TypeAdapter.getTypeDescription(wildcardType.getUpperBounds()[0])));
        } else {
            bound = Optional.empty();
        }
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        if (bound.isEmpty()) return Stream.empty();
        var inner = bound.get();
        if (inner.left().isPresent()) return inner.left().get().stream();
        if (inner.right().isPresent()) return inner.right().get().stream();

        throw new RuntimeException("Impossible");
    }
}
