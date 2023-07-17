package com.probejs.jdoc.java;

import com.probejs.jdoc.java.type.ITypeInfo;
import com.probejs.jdoc.java.type.TypeInfoParameterized;
import com.probejs.jdoc.java.type.TypeInfoVariable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Walker {
    private final Set<Class<?>> initial;
    private boolean walkMethod = true;
    private boolean walkField = true;
    private boolean walkSuper = true;
    private boolean walkType = true;


    public Walker(Set<Class<?>> initial) {
        this.initial = initial;
    }

    public void setWalkField(boolean walkField) {
        this.walkField = walkField;
    }

    public void setWalkMethod(boolean walkMethod) {
        this.walkMethod = walkMethod;
    }

    public void setWalkSuper(boolean walkSuper) {
        this.walkSuper = walkSuper;
    }

    public void setWalkType(boolean walkType) {
        this.walkType = walkType;
    }

    private Set<Class<?>> walkType(ITypeInfo type) {
        Set<Class<?>> result = new HashSet<>();
        if (type instanceof TypeInfoParameterized parType && walkType) {
            parType.getParamTypes().forEach(info -> result.addAll(walkType(info)));
        }
        if (type instanceof TypeInfoVariable variable) {
            for (ITypeInfo bound : variable.getBounds()) {
                result.add(bound.getResolvedClass());
            }
        } else {
            result.add(type.getResolvedClass());
        }
        result.removeIf(Objects::isNull);
        return result;
    }

    private Set<Class<?>> touch(Set<Class<?>> classes) {
        Set<Class<?>> result = new HashSet<>();
        for (Class<?> clazz : classes) {
            ClassInfo info = ClassInfo.getOrCache(clazz);

            if (walkSuper) {
                ClassInfo superclass = info.getSuperClass();
                if (superclass != null)
                    result.add(superclass.getClazzRaw());
                info.getInterfaces().stream().map(ClassInfo::getClazzRaw).forEach(result::add);
            }
            if (walkField)
                info.getFieldInfo().forEach(f -> {
                    result.addAll(walkType(f.getType()));
                    if (f.isStatic()) {
                        Object value = f.getStaticValue();
                        if (value != null) {
                            Class<?> valueClazz = value.getClass();
                            if (!valueClazz.isAnonymousClass() &&
                                    !valueClazz.isSynthetic() &&
                                    !valueClazz.isArray()
                            )
                                result.add(value.getClass());
                        }
                    }
                });
            if (walkMethod)
                info.getMethodInfo().forEach(m -> {
                    result.addAll(walkType(m.getReturnType()));
                    m.getParams().forEach(p -> result.addAll(walkType(p.getType())));
                });

        }
        result.removeIf(Objects::isNull);
        return result;
    }

    public Set<Class<?>> walk() {
        Set<Class<?>> result = new HashSet<>(initial);
        Set<Class<?>> current = touch(result);

        while (!current.isEmpty()) {
            result.addAll(current);
            current = touch(current)
                    .parallelStream()
                    .filter(c -> !result.contains(c))
                    .collect(Collectors.toSet());
        }
        result.removeIf(Class::isSynthetic);
        return result;
    }
}
