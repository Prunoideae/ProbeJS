package moe.wolfgirl.probejs.lang.java.clazz.members;

import dev.latvian.mods.rhino.CachedFieldInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.lang.java.base.AnnotationHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldInfo extends AnnotationHolder {
    public final String name;
    public final TypeInfo type;
    public final FieldAttributes attributes;

    public FieldInfo(String name, CachedFieldInfo fieldInfo, Field original) {
        super(original.getAnnotations());
        this.name = name;
        this.type = fieldInfo.getType();
        this.attributes = new FieldAttributes(original);
    }

    public static class FieldAttributes {
        public final boolean isFinal;
        public final boolean isStatic;
        private final Field field;

        public FieldAttributes(Field field) {
            int modifiers = field.getModifiers();
            this.isFinal = Modifier.isFinal(modifiers);
            this.isStatic = Modifier.isStatic(modifiers);
            this.field = field;
        }

        public Object getStaticValue() throws IllegalAccessException {
            if (!isStatic) throw new RuntimeException("The field is not static!");
            return field.get(null);
        }
    }
}
