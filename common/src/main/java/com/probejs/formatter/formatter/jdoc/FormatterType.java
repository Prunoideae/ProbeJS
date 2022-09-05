package com.probejs.formatter.formatter.jdoc;

import com.probejs.ProbeJS;
import com.probejs.formatter.NameResolver;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.jdoc.property.PropertyType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class FormatterType<T extends PropertyType<T>> extends DocumentFormatter<T> {
    public static Map<Class<? extends PropertyType<?>>, Function<PropertyType<?>, FormatterType<?>>> FORMATTER_REGISTRY = new HashMap<>();

    protected boolean underscored;

    public FormatterType(T document) {
        super(document);
    }

    @Override
    public boolean hasComment() {
        return false;
    }

    public void setUnderscored(boolean underscored) {
        this.underscored = underscored;
    }

    public FormatterType<T> underscored() {
        setUnderscored(true);
        return this;
    }

    public static abstract class Named<T extends PropertyType.Named<T>> extends FormatterType<T> {
        public Named(T type) {
            super(type);
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(" ".repeat(indent) + document.getTypeName() + (underscored ? "_" : ""));
        }
    }

    public static class Clazz extends Named<PropertyType.Clazz> {
        public Clazz(PropertyType.Clazz type) {
            super(type);
        }

        @Override
        public void setUnderscored(boolean underscored) {
            //Shield the underscore if the type is primitive
            if (!NameResolver.resolvedPrimitives.contains(document.getTypeName()))
                super.setUnderscored(underscored);
        }
    }

    public static class Native extends Named<PropertyType.Native> {
        public Native(PropertyType.Native type) {
            super(type);
        }

        @Override
        public void setUnderscored(boolean underscored) {
            //Shield the underscore since it's builtin in JS/TS
        }
    }

    public static class Variable extends Named<PropertyType.Variable> {
        public Variable(PropertyType.Variable type) {
            super(type);
        }
    }

    public static abstract class Joint<T extends PropertyType.Joint<T>> extends FormatterType<T> {
        private final List<FormatterType<?>> types;

        public Joint(T type) {
            super(type);
            types = type.getTypes().stream().map(FormatterType::getFormatter).collect(Collectors.toList());
        }

        @Override
        public void setUnderscored(boolean underscored) {
            types.forEach(type -> type.setUnderscored(underscored));
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(
                    " ".repeat(indent) + types.stream()
                            .map(t -> (t instanceof FormatterType.Joint ? "(%s)" : "%s").formatted(t.format()))
                            .collect(Collectors.joining(document.getDelimiter()))
            );
        }
    }

    public static class Union extends Joint<PropertyType.Union> {
        public Union(PropertyType.Union type) {
            super(type);
        }
    }

    public static class Intersection extends Joint<PropertyType.Intersection> {
        public Intersection(PropertyType.Intersection type) {
            super(type);
        }
    }

    public static class Parameterized extends FormatterType<PropertyType.Parameterized> {
        private final FormatterType<?> base;
        private final List<FormatterType<?>> params;

        public Parameterized(PropertyType.Parameterized type) {
            super(type);
            base = FormatterType.getFormatter(type.getBase());
            params = type.getParams().stream().map(FormatterType::getFormatter).collect(Collectors.toList());
        }

        @Override
        public void setUnderscored(boolean underscored) {
            base.setUnderscored(underscored);
            params.forEach(param -> param.setUnderscored(underscored));
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(
                    "%s<%s>".formatted(
                            base.format(),
                            params.stream().map(IFormatter::formatFirst).collect(Collectors.joining(", "))
                    ));
        }
    }

    public static class Array extends FormatterType<PropertyType.Array> {
        private final FormatterType<?> formatter;

        public Array(PropertyType.Array type) {
            super(type);
            formatter = FormatterType.getFormatter(type.getComponent());
        }

        @Override
        public void setUnderscored(boolean underscored) {
            formatter.setUnderscored(underscored);
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(formatter.formatFirst() + "[]");
        }
    }

    public static class JSObject extends FormatterType<PropertyType.JSObject> {
        private final Map<Object, FormatterType<?>> keyValues = new HashMap<>();

        public JSObject(PropertyType.JSObject document) {
            super(document);
            document.getKeyValues()
                    .forEach((key, value) -> keyValues.put(
                            key instanceof PropertyType<?> type ?
                                    FormatterType.getFormatter(type).underscored() :
                                    key,
                            FormatterType.getFormatter(value)));
        }

        @Override
        public void setUnderscored(boolean underscored) {
            keyValues.values().forEach(value -> value.setUnderscored(underscored));
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of("{%s}".formatted(keyValues.entrySet().stream().map(pair -> {
                Object key = pair.getKey();
                FormatterType<?> value = pair.getValue();
                return "%s: %s".formatted(
                        key instanceof FormatterType<?> formatter ?
                                "[key: (%s) & string]".formatted(formatter.formatFirst()) :
                                ProbeJS.GSON.toJson(key),
                        value.formatFirst());
            }).collect(Collectors.joining(", "))));
        }
    }

    public static FormatterType<?> getFormatter(PropertyType<?> type) {
        Function<PropertyType<?>, FormatterType<?>> constructor = FORMATTER_REGISTRY.get(type.getClass());
        if (constructor != null)
            return constructor.apply(type);
        return new Native(new PropertyType.Native("any"));
    }

    @SuppressWarnings("unchecked")
    public static <T extends PropertyType<T>> void addFormatter(Class<T> clazz, Function<T, FormatterType<T>> constructor) {
        FORMATTER_REGISTRY.put(clazz, type -> constructor.apply((T) type));
    }

    public static void init() {
        addFormatter(PropertyType.Clazz.class, Clazz::new);
        addFormatter(PropertyType.Native.class, Native::new);
        addFormatter(PropertyType.Variable.class, Variable::new);
        addFormatter(PropertyType.Union.class, Union::new);
        addFormatter(PropertyType.Intersection.class, Intersection::new);
        addFormatter(PropertyType.Parameterized.class, Parameterized::new);
        addFormatter(PropertyType.Array.class, Array::new);
        addFormatter(PropertyType.JSObject.class, JSObject::new);
    }
}
