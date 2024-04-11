package com.probejs.next.java.clazz.members;

import com.probejs.next.java.base.AnnotationHolder;
import com.probejs.next.java.base.ClassPathProvider;
import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.java.type.TypeAdapter;
import com.probejs.next.java.type.TypeDescriptor;

import java.lang.reflect.Parameter;
import java.util.Collection;

public class ParamInfo extends AnnotationHolder implements ClassPathProvider {
    public final String name;
    public final TypeDescriptor type;
    public final boolean varArgs;

    public ParamInfo(Parameter parameter) {
        super(parameter.getAnnotations());
        this.name = parameter.getName();
        this.type = TypeAdapter.getTypeDescription(parameter.getAnnotatedType());
        this.varArgs = parameter.isVarArgs();
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        return type.getClassPaths();
    }
}
