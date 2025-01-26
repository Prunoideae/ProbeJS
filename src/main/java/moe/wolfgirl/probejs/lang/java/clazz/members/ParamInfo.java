package moe.wolfgirl.probejs.lang.java.clazz.members;

import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.lang.java.base.AnnotationHolder;

import java.lang.reflect.Parameter;

public class ParamInfo extends AnnotationHolder {
    public String name;
    public TypeInfo type;
    public final boolean varArgs;

    public ParamInfo(Parameter parameter) {
        super(parameter.getAnnotations());
        this.name = parameter.getName();
        this.type = TypeInfo.of(parameter.getParameterizedType());
        this.varArgs = parameter.isVarArgs();
    }

}
