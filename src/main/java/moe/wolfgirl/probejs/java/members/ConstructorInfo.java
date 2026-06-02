package moe.wolfgirl.probejs.java.members;

import dev.latvian.mods.rhino.CachedConstructorInfo;
import dev.latvian.mods.rhino.CachedExecutableInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.java.members.other.ClassProvider;
import moe.wolfgirl.probejs.java.members.other.HasAnnotation;
import moe.wolfgirl.probejs.java.members.other.HasTypeVariable;
import moe.wolfgirl.probejs.java.members.other.ParamInfo;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;
import java.util.*;

public record ConstructorInfo(List<ParamInfo> params,
                              Annotation[] annotations,
                              TypeVariable<?>[] typeVariables,
                              String descriptor
) implements HasAnnotation, HasTypeVariable, ClassProvider {
    public static ConstructorInfo resolve(CachedConstructorInfo constructorInfo, Map<String, TypeInfo> typeRemap) {
        var typeRemapNoLocal = new HashMap<>(typeRemap);
        for (TypeVariable<?> typeVariable : constructorInfo.getCached().getTypeParameters()) {
            typeRemapNoLocal.remove(typeVariable.getName());
        }

        return new ConstructorInfo(ParamInfo.resolve(constructorInfo, typeRemapNoLocal),
                constructorInfo.getCached().getAnnotations(),
                constructorInfo.getCached().getTypeParameters(),
                Type.getConstructorDescriptor(constructorInfo.getCached())
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
