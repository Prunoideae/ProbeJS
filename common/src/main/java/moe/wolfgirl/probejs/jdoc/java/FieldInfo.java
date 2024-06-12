package moe.wolfgirl.probejs.jdoc.java;


import moe.wolfgirl.probejs.jdoc.java.type.ITypeInfo;
import moe.wolfgirl.probejs.jdoc.java.type.InfoTypeResolver;
import moe.wolfgirl.probejs.util.Util;
import dev.latvian.mods.rhino.JavaMembers;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class FieldInfo {
    public final String name;
    public final int modifiers;
    public final boolean shouldHide;
    public final Object value;
    public ITypeInfo info;
    public final List<Annotation> annotations;

    public FieldInfo(JavaMembers.FieldInfo fieldInfo) {
        Field field = fieldInfo.field;
        name = fieldInfo.name.isEmpty() ? field.getName() : fieldInfo.name;
        modifiers = field.getModifiers();
        shouldHide = field.getAnnotation(HideFromJS.class) != null;
        info = InfoTypeResolver.resolveType(field.getGenericType());
        value = Util.tryOrDefault(() -> isStatic() ? field.get(null) : null, null);
        annotations = List.of(field.getAnnotations());
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    public boolean isTransient() {
        return Modifier.isTransient(modifiers);
    }

    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    public String getName() {
        return name;
    }

    public boolean shouldHide() {
        return shouldHide;
    }

    public ITypeInfo getType() {
        return info;
    }

    public Object getStaticValue() {
        return value;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setTypeInfo(ITypeInfo info) {
        this.info = info;
    }

}
