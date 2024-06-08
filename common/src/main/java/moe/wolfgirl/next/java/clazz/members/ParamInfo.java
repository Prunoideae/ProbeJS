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
    public String name;
    public TypeDescriptor type;
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
