package moe.wolfgirl.probejs.java.members;

import dev.latvian.mods.rhino.CachedMethodInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.java.members.other.ClassProvider;
import moe.wolfgirl.probejs.java.members.other.HasAnnotation;
import moe.wolfgirl.probejs.java.members.other.HasTypeVariable;
import moe.wolfgirl.probejs.java.members.other.ParamInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.*;

public record MethodInfo(String name,
                         List<ParamInfo> params,
                         TypeInfo returnType,
                         boolean isStatic, boolean isAbstract, boolean isSynthetic,
                         Annotation[] annotations,
                         TypeVariable<?>[] typeVariables,
                         ClassPath declaringClass
) implements HasAnnotation, HasTypeVariable, ClassProvider {
    public static MethodInfo resolve(CachedMethodInfo methodInfo, Map<String, TypeInfo> typeRemap) {
        var typeRemapNoLocal = new HashMap<>(typeRemap);
        for (TypeVariable<?> typeVariable : methodInfo.getCached().getTypeParameters()) {
            typeRemapNoLocal.remove(typeVariable.getName());
        }

        return new MethodInfo(methodInfo.getName(),
                ParamInfo.resolve(methodInfo, typeRemapNoLocal),
                ClassInfo.remapType(methodInfo.getReturnType(), typeRemapNoLocal),
                methodInfo.isStatic,
                Modifier.isAbstract(methodInfo.modifiers),
                methodInfo.getCached().isSynthetic(),
                methodInfo.getCached().getAnnotations(),
                methodInfo.getCached().getTypeParameters(),
                new ClassPath(methodInfo.getDeclaringClass().type)
        );
    }

    @Override
    public Collection<Class<?>> getReferredClasses() {
        Set<Class<?>> classes = new HashSet<>();
        for (ParamInfo param : params) {
            classes.addAll(param.getReferredClasses());
        }
        classes.addAll(returnType.getContainedComponentClasses());
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
