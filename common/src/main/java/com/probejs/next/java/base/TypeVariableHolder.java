package com.probejs.next.java.base;

import com.probejs.next.java.type.impl.VariableType;

import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TypeVariableHolder extends AnnotationHolder {
    private final List<VariableType> variableTypes;

    public TypeVariableHolder(TypeVariable<?>[] variables, Annotation[] annotations) {
        super(annotations);
        this.variableTypes = Arrays.stream(variables).map(VariableType::new).collect(Collectors.toList());
    }

    public List<VariableType> getVariableTypes() {
        return variableTypes;
    }
}
