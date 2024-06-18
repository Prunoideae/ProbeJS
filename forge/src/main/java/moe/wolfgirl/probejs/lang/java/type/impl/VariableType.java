package moe.wolfgirl.probejs.lang.java.type.impl;

import moe.wolfgirl.probejs.lang.java.type.TypeAdapter;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariableType extends TypeDescriptor {
    public String symbol;
    public List<TypeDescriptor> descriptors;

    public VariableType(AnnotatedTypeVariable typeVariable) {
        this(typeVariable, true);
    }

    public VariableType(TypeVariable<?> typeVariable) {
        this(typeVariable, true);
    }

    public VariableType(AnnotatedTypeVariable typeVariable, boolean checkBounds) {
        super(typeVariable.getAnnotations());
        this.symbol = ((TypeVariable<?>) typeVariable.getType()).getName();
        this.descriptors = checkBounds ? Arrays.stream(typeVariable.getAnnotatedBounds())
                // Filter out unannotated Object here
                .filter(bound -> !bound.getType().equals(Object.class))
                .map(TypeAdapter::getTypeDescription)
                .collect(Collectors.toList()) : new ArrayList<>();
    }

    public VariableType(TypeVariable<?> typeVariable, boolean checkBounds) {
        super(new Annotation[0]);
        this.symbol = typeVariable.getName();
        this.descriptors = checkBounds ? Arrays.stream(typeVariable.getAnnotatedBounds())
                // Filter out unannotated Object here
                .filter(bound -> !bound.getType().equals(Object.class))
                .map(TypeAdapter::getTypeDescription)
                .collect(Collectors.toList()) : new ArrayList<>();
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
