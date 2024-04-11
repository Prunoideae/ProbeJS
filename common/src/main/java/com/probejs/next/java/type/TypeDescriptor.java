package com.probejs.next.java.type;

import com.probejs.next.java.base.AnnotationHolder;
import com.probejs.next.java.clazz.ClassPath;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TypeDescriptor extends AnnotationHolder {
    public TypeDescriptor(Annotation[] annotations) {
        super(annotations);
    }

    /**
     * Iterate through contained types.
     * <br>
     * For simple classes, the class yields itself.
     */
    public abstract Stream<TypeDescriptor> stream();

    /**
     * Gets the packages required to use the type.
     */
    public Collection<ClassPath> getPackages() {
        return stream().flatMap(t -> t.getPackages().stream()).collect(Collectors.toSet());
    }

    /**
     * Gets the classes involved in the type.
     */
    public Collection<Class<?>> getClasses() {
        return stream().flatMap(t -> t.getClasses().stream()).collect(Collectors.toSet());
    }
}
