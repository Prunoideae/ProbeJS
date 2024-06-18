package moe.wolfgirl.probejs.lang.java.type.impl;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.type.TypeAdapter;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.GenericArrayType;
import java.util.Collection;
import java.util.stream.Stream;

public class ArrayType extends TypeDescriptor {
    public TypeDescriptor component;

    public ArrayType(AnnotatedArrayType arrayType) {
        super(arrayType.getAnnotations());
        this.component = TypeAdapter.getTypeDescription(arrayType.getAnnotatedGenericComponentType());
    }

    public ArrayType(GenericArrayType arrayType) {
        super(new Annotation[]{});
        this.component = TypeAdapter.getTypeDescription(arrayType.getGenericComponentType());
    }

    public ArrayType(TypeDescriptor arrayType) {
        super(new Annotation[]{});
        this.component = arrayType;
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return component.stream();
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        return component.getClassPaths();
    }

    @Override
    public int hashCode() {
        return component.hashCode() * 31;
    }
}
