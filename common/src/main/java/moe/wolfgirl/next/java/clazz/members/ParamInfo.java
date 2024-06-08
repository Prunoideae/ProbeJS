package moe.wolfgirl.next.java.clazz.members;

import moe.wolfgirl.next.java.base.AnnotationHolder;
import moe.wolfgirl.next.java.base.ClassPathProvider;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.java.type.TypeAdapter;
import moe.wolfgirl.next.java.type.TypeDescriptor;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;

public class ParamInfo extends AnnotationHolder implements ClassPathProvider {
    public final String name;
    public final TypeDescriptor type;
    public final boolean varArgs;

    public ParamInfo(Parameter parameter, Type actual) {
        super(parameter.getAnnotations());
        this.name = parameter.getName();
        this.type = actual == null ?
                TypeAdapter.getTypeDescription(parameter.getAnnotatedType()) :
                TypeAdapter.getTypeDescription(actual);
        this.varArgs = parameter.isVarArgs();
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        return type.getClassPaths();
    }
}
