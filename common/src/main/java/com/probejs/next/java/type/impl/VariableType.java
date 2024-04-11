package com.probejs.next.java.type.impl;

import com.probejs.next.java.type.TypeAdapter;
import com.probejs.next.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariableType extends TypeDescriptor {
    private final String symbol;
    private final List<TypeDescriptor> descriptors;

    public VariableType(AnnotatedTypeVariable typeVariable) {
        super(typeVariable.getAnnotations());
        this.symbol = ((TypeVariable<?>) typeVariable.getType()).getName();
        this.descriptors = Arrays.stream(typeVariable.getAnnotatedBounds())
                // Filter out unannotated Object here
                .filter(bound -> !bound.getType().equals(Object.class))
                .map(TypeAdapter::getTypeDescription)
                .collect(Collectors.toList());
    }

    public VariableType(TypeVariable<?> typeVariable) {
        super(new Annotation[0]);
        this.symbol = typeVariable.getName();
        this.descriptors = Arrays.stream(typeVariable.getAnnotatedBounds())
                // Filter out unannotated Object here
                .filter(bound -> !bound.getType().equals(Object.class))
                .map(TypeAdapter::getTypeDescription)
                .collect(Collectors.toList());
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return descriptors.stream().flatMap(TypeDescriptor::stream);
    }

    public String getSymbol() {
        return symbol;
    }

    public List<TypeDescriptor> getDescriptors() {
        return descriptors;
    }
}
