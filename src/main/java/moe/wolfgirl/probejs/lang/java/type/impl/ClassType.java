package moe.wolfgirl.probejs.lang.java.type.impl;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ClassType extends TypeDescriptor {
    public final ClassPath classPath;
    public final Class<?> clazz;

    public ClassType(AnnotatedType type) {
        super(type.getAnnotations());
        clazz = (Class<?>) type.getType();
        classPath = new ClassPath(clazz);
    }

    public ClassType(Type type) {
        super(new Annotation[]{});
        clazz = (Class<?>) type;
        classPath = new ClassPath(clazz);
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return Stream.of(this);
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        return List.of(classPath);
    }

    @Override
    public Collection<Class<?>> getClasses() {
        return List.of(clazz);
    }

    @Override
    public int hashCode() {
        return classPath.hashCode();
    }
}
