package moe.wolfgirl.next.java.type.impl;

import com.mojang.datafixers.util.Either;
import moe.wolfgirl.next.java.type.TypeAdapter;
import moe.wolfgirl.next.java.type.TypeDescriptor;

import java.lang.reflect.AnnotatedWildcardType;
import java.util.Optional;
import java.util.stream.Stream;

public class WildcardType extends TypeDescriptor {
    private final Optional<Either<TypeDescriptor, TypeDescriptor>> bound;

    public WildcardType(AnnotatedWildcardType wildcardType) {
        super(wildcardType.getAnnotations());
        if (wildcardType.getAnnotatedLowerBounds().length != 0) {
            bound = Optional.of(Either.left(TypeAdapter.getTypeDescription(wildcardType.getAnnotatedLowerBounds()[0])));
        } else if (!wildcardType.getAnnotatedUpperBounds()[0].getType().equals(Object.class)) {
            bound = Optional.of(Either.right(TypeAdapter.getTypeDescription(wildcardType.getAnnotatedUpperBounds()[0])));
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
