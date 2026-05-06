package moe.wolfgirl.probejs.java.members;

import dev.latvian.mods.rhino.CachedConstructorInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.java.members.other.ClassProvider;
import moe.wolfgirl.probejs.java.members.other.HasAnnotation;
import moe.wolfgirl.probejs.java.members.other.HasTypeVariable;
import moe.wolfgirl.probejs.java.members.other.ParamInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;
import java.util.*;

public record ConstructorInfo(List<ParamInfo> params,
                              Annotation[] annotations,
                              TypeVariable<?>[] typeVariables
) implements HasAnnotation, HasTypeVariable, ClassProvider {
    public ConstructorInfo(CachedConstructorInfo constructorInfo, Map<String, TypeInfo> typeRemap) {
        this(ParamInfo.resolve(constructorInfo, typeRemap),
                constructorInfo.getCached().getAnnotations(),
                constructorInfo.getCached().getTypeParameters()
        );
    }

    @Override
    public Collection<Class<?>> getReferredClasses() {
        Set<Class<?>> classes = new HashSet<>();
        for (ParamInfo param : params) {
            classes.addAll(param.getReferredClasses());
        }
        for (TypeVariable<?> typeVariable : typeVariables) {
            classes.addAll(TypeInfo.of(typeVariable).getContainedComponentClasses());
        }
        return classes;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public TypeVariable<?>[] getTypeVariables() {
        return typeVariables;
    }
}
