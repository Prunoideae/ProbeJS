package moe.wolfgirl.next.java.type.impl;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.java.type.TypeDescriptor;
import moe.wolfgirl.next.utils.RemapperUtils;

import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ClassType extends TypeDescriptor {
    private final ClassPath pack;
    private final Class<?> clazz;

    public ClassType(AnnotatedType type) {
        super(type.getAnnotations());
        clazz = (Class<?>) type.getType();
        pack = new ClassPath(RemapperUtils.getRemappedClassName(clazz));
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return Stream.of(this);
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        return List.of(pack);
    }

    @Override
    public Collection<Class<?>> getClasses() {
        return List.of(clazz);
    }

    @Override
    public int hashCode() {
        return pack.hashCode();
    }
}
