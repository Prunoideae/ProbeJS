package com.probejs.formatter.formatter.jdoc;

import com.probejs.ProbeJS;
import com.probejs.formatter.NameResolver;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.jdoc.property.PropertyUnderscored;
import com.probejs.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class FormatterType<T extends PropertyType<T>> extends DocumentFormatter<T> {
    public static Map<Class<? extends PropertyType<?>>, Function<PropertyType<?>, FormatterType<?>>> FORMATTER_REGISTRY = new HashMap<>();

    protected boolean underscored = false;

    public FormatterType(T document) {
        super(document);
        document.findProperty(PropertyUnderscored.class).ifPresent(property -> underscored(property.isUnderscored()));
    }

    @Override
    public boolean hasComment() {
        return false;
    }

    @Override
    public boolean canHide() {
        return false;
    }

    public FormatterType<T> underscored(boolean underscored) {
        if (!document.hasProperty(PropertyUnderscored.class))
            this.underscored = underscored;
        return this;
    }

    public FormatterType<T> underscored() {
        underscored(true);
        return this;
    }

    public static abstract class Named<T extends PropertyType.Named<T>> extends FormatterType<T> {
        public Named(T type) {
            super(type);
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(Util.indent(indent) + document.getTypeName() + (underscored ? "_" : ""));
        }
    }

    public static class Clazz extends Named<PropertyType.Clazz> {
        public Clazz(PropertyType.Clazz type) {
            super(type);
        }

        @Override
        public Clazz underscored(boolean underscored) {
            //Shield the underscore if the type is primitive
            if (!NameResolver.resolvedPrimitives.contains(document.getClassName()))
                super.underscored(underscored);
            return this;
        }
    }

    public static class Native extends Named<PropertyType.Native> {
        public Native(PropertyType.Native type) {
            super(type);
        }

        @Override
        public Native underscored(boolean underscored) {
            //Shield the underscore since it's builtin in JS/TS
            return this;
        }
    }

    public static class Variable extends Named<PropertyType.Variable> {
        public Variable(PropertyType.Variable type) {
            super(type);
        }

        @Override
        public Variable underscored(boolean underscored) {
            //Shield the underscore since it's generic name
            return this;
        }
    }

    public static abstract class Joint<T extends PropertyType.Joint<T>> extends FormatterType<T> {
        private final List<FormatterType<?>> types;

        public Joint(T type) {
            super(type);
            types = type.getTypes().stream().map(Serde::getTypeFormatter).collect(Collectors.toList());
        }

        @Override
        public Joint<T> underscored(boolean underscored) {
            if (!document.hasProperty(PropertyUnderscored.class))
                types.forEach(type -> type.underscored(underscored));
            return this;
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(
                    Util.indent(indent) + types.stream()
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
            base = Serde.getTypeFormatter(type.getBase());
            params = type.getParams().stream().map(Serde::getTypeFormatter).collect(Collectors.toList());
        }

        @Override
        public Parameterized underscored(boolean underscored) {
            base.underscored(underscored);
            params.forEach(param -> param.underscored(underscored));
            return this;
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            String baseString = base.formatFirst();
            return List.of(
                    !baseString.equals("any") ? "%s<%s>".formatted(
                            baseString,
                            params.stream().map(IFormatter::formatFirst).collect(Collectors.joining(", "))
                    ) : "any");
        }
    }

    public static class Array extends FormatterType<PropertyType.Array> {
        private final FormatterType<?> formatter;

        public Array(PropertyType.Array type) {
            super(type);
            formatter = Serde.getTypeFormatter(type.getComponent());
        }

        @Override
        public Array underscored(boolean underscored) {
            formatter.underscored(underscored);
            return this;
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
                                    Serde.getTypeFormatter(type).underscored() :
                                    key,
                            Serde.getTypeFormatter(value)));
        }

        @Override
        public JSObject underscored(boolean underscored) {
            keyValues.values().forEach(value -> value.underscored(underscored));
            return this;
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of("{%s}".formatted(keyValues.entrySet().stream().map(pair -> {
                Object key = pair.getKey();
                FormatterType<?> value = pair.getValue();
                return "%s: %s".formatted(
                        key instanceof FormatterType<?> formatter ?
                                "[key: (%s)]".formatted(formatter.formatFirst()) :
                                ProbeJS.GSON.toJson(key),
                        value.formatFirst());
            }).collect(Collectors.joining(", "))));
        }
    }

    public static class JSArray extends FormatterType<PropertyType.JSArray> {
        private final List<FormatterType<?>> types = new ArrayList<>();

        public JSArray(PropertyType.JSArray document) {
            super(document);
            document.getTypes().forEach(type -> types.add(Serde.getTypeFormatter(type)));
        }

        @Override
        public FormatterType<PropertyType.JSArray> underscored(boolean underscored) {
            types.forEach(type -> type.underscored(underscored));
            return this;
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of("[%s]".formatted(types.stream().map(IFormatter::formatFirst).collect(Collectors.joining(", "))));
        }
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
        addFormatter(PropertyType.JSArray.class, JSArray::new);
    }
}
