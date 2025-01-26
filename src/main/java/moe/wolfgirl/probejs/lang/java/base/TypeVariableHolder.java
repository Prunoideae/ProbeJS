package moe.wolfgirl.probejs.lang.java.base;

import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.type.VariableTypeInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TypeVariableHolder extends AnnotationHolder {
    public final List<VariableTypeInfo> variableTypes;

    public TypeVariableHolder(TypeVariable<?>[] variables, Annotation[] annotations) {
        super(annotations);
        this.variableTypes = Arrays.stream(variables)
                .map(TypeInfo::of)
                .map(t -> (VariableTypeInfo) t)
                .collect(Collectors.toList());
    }
}
