package moe.wolfgirl.probejs.lang.java.base;

import moe.wolfgirl.probejs.lang.java.type.impl.VariableType;

import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TypeVariableHolder extends AnnotationHolder {
    public final List<VariableType> variableTypes;

    public TypeVariableHolder(TypeVariable<?>[] variables, Annotation[] annotations) {
        super(annotations);
        this.variableTypes = Arrays.stream(variables).map(VariableType::new).collect(Collectors.toList());
    }
}
