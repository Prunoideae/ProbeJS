package moe.wolfgirl.probejs.next.java.members;

import dev.latvian.mods.rhino.CachedFieldInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.next.java.members.other.ClassProvider;
import moe.wolfgirl.probejs.next.java.members.other.HasAnnotation;

import java.lang.annotation.Annotation;
import java.util.Collection;

public record FieldInfo(String name, TypeInfo type,
                        boolean isStatic, boolean isFinal,
                        Annotation[] annotations
) implements HasAnnotation, ClassProvider {

    public FieldInfo(CachedFieldInfo fieldInfo) {
        this(fieldInfo.getName(), fieldInfo.getType(),
                fieldInfo.isStatic, fieldInfo.isFinal,
                fieldInfo.getCached().getAnnotations()
        );
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public Collection<Class<?>> getReferredClasses() {
        return type.getContainedComponentClasses();
    }
}
