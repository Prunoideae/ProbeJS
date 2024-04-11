package com.probejs.next.java.clazz.members;

import com.probejs.next.java.base.ClassPathProvider;
import com.probejs.next.java.base.TypeVariableHolder;
import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.java.type.impl.VariableType;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class ConstructorInfo extends TypeVariableHolder implements ClassPathProvider {

    public final List<ParamInfo> params;

    public ConstructorInfo(Constructor<?> constructor) {
        super(constructor.getTypeParameters(), constructor.getAnnotations());
        this.params = Arrays.stream(constructor.getParameters()).map(ParamInfo::new).collect(Collectors.toList());
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (ParamInfo param : params) {
            paths.addAll(param.getClassPaths());
        }
        for (VariableType variableType : variableTypes) {
            paths.addAll(variableType.getClassPaths());
        }
        return paths;
    }
}
