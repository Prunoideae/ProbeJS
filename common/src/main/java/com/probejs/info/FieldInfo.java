package com.probejs.info;


import com.probejs.formatter.SpecialTypes;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.InfoTypeResolver;
import com.probejs.util.Util;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

public class FieldInfo {
    private final String name;
    private final int modifiers;
    private final boolean shouldHide;
    private final Object value;
    private ITypeInfo info;
    private final List<Annotation> annotations;

    private static String getRemappedOrDefault(Field field) {
        String s = MethodInfo.RUNTIME.getMappedField(field.getDeclaringClass(), field);
        if (s.equals(""))
            s = field.getName();
        if (field.isAnnotationPresent(RemapForJS.class))
            s = field.getAnnotation(RemapForJS.class).value();
        return s.isEmpty() ? field.getName() : s;
    }

    public FieldInfo(Field field) {
        name = getRemappedOrDefault(field);
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
