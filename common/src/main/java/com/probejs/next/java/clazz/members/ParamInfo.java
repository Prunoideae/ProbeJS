package com.probejs.next.java.clazz.members;

import com.probejs.next.java.base.AnnotationHolder;
import com.probejs.next.java.type.TypeAdapter;
import com.probejs.next.java.type.TypeDescriptor;

import java.lang.reflect.Parameter;

public class ParamInfo extends AnnotationHolder {
    private final String name;
    private final TypeDescriptor type;
    private final boolean varArgs;

    public ParamInfo(Parameter parameter) {
        super(parameter.getAnnotations());
        this.name = parameter.getName();
        this.type = TypeAdapter.getTypeDescription(parameter.getAnnotatedType());
        this.varArgs = parameter.isVarArgs();
    }

    public String getName() {
        return name;
    }

    public TypeDescriptor getType() {
        return type;
    }

    public boolean isVarArgs() {
        return varArgs;
    }
}
