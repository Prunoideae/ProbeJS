package moe.wolfgirl.probejs.lang.java.clazz.members;

import moe.wolfgirl.probejs.lang.java.base.AnnotationHolder;
import moe.wolfgirl.probejs.lang.java.base.ClassPathProvider;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.type.TypeAdapter;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;

import java.lang.reflect.Parameter;
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
