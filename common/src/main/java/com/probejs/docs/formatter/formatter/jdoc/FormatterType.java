package com.probejs.docs.formatter.formatter.jdoc;

import com.probejs.docs.formatter.NameResolver;
import com.probejs.docs.formatter.formatter.IFormatter;
import com.probejs.jdoc.java.type.ITypeInfo;
import com.probejs.jdoc.java.type.TypeInfoClass;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.jdoc.property.PropertyUnderscored;
import com.probejs.util.Util;
import dev.latvian.mods.kubejs.util.ClassWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class FormatterType<T extends PropertyType<T>> extends DocumentFormatter<T> {
    public static Map<Class<? extends PropertyType<?>>, Function<PropertyType<?>, FormatterType<?>>> FORMATTER_REGISTRY = new HashMap<>();

    protected Boolean underscored = null;

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

    public Boolean getUnderscored() {
        return underscored != null && underscored;
    }

    public FormatterType<T> underscored(boolean underscored) {
        if (this.underscored == null)
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
            return List.of(Util.indent(indent) + document.getTypeName() + (getUnderscored() ? "_" : ""));
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
        private final List<FormatterType<?>> bounds;

        public Variable(PropertyType.Variable type) {
            super(type);
            bounds = type.getBounds().stream().map(Serde::getTypeFormatter).collect(Collectors.toList());
        }

        @Override
        public Variable underscored(boolean underscored) {
            //Shield the underscore since it's generic name
            return this;
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            bounds.removeIf(formatterType -> formatterType.formatFirst().equals("any"));
            if (bounds.isEmpty()) {
                return List.of(document.getTypeName());
            } else {
                return List.of("%s extends %s".formatted(document.getTypeName(), bounds.stream().map(IFormatter::formatParamVariable).collect(Collectors.joining(" & "))));
            }
        }

        @Override
        public String formatParamVariable() {
            return document.getTypeName();
        }

        @Override
        public String formatFieldVariable() {
            return document.getTypeName();
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
        public String formatAdapted(Function<IFormatter, String> formatterMethod) {
            return types.stream().map(t -> (t instanceof FormatterType.Joint ? "(%s)" : "%s").formatted(formatterMethod.apply(t)))
                    .collect(Collectors.joining(document.getDelimiter()));
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(Util.indent(indent) + formatAdapted(IFormatter::formatFirst));
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
        private static final ITypeInfo CLASS_TYPE = new TypeInfoClass(Class.class);
        private static final ITypeInfo CLASS_WRAPPER_TYPE = new TypeInfoClass(ClassWrapper.class);

        private final FormatterType<?> base;
        private final List<FormatterType<?>> params;

        public Parameterized(PropertyType.Parameterized type) {
            super(type);
            base = Serde.getTypeFormatter(type.getBase());
            params = type.getParams().stream().map(Serde::getTypeFormatter).map(formatter -> formatter.underscored(false)).collect(Collectors.toList());
        }

        @Override
        public Parameterized underscored(boolean underscored) {
            base.underscored(underscored);
            params.forEach(param -> param.underscored(false));
            return this;
        }

        @Override
        public String formatAdapted(Function<IFormatter, String> formatterMethod) {
            if (document.getBase().equalsToJavaType(CLASS_TYPE) || document.getBase().equalsToJavaType(CLASS_WRAPPER_TYPE)) {
                if (params.get(0) instanceof Clazz) {
                    return "typeof %s".formatted(formatterMethod.apply(params.get(0)));
                }
                return formatterMethod.apply(params.get(0));
            }
            String baseString = formatterMethod.apply(base);
            if (base instanceof FormatterType.Joint<?>)
                baseString = "(%s)".formatted(baseString);
            return !baseString.equals("any") ? "%s<%s>".formatted(
                    baseString,
                    params.stream().map(formatterMethod).collect(Collectors.joining(", "))
            ) : "any";
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(formatAdapted(IFormatter::formatFirst));
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
            if (formatter instanceof FormatterType.Joint<?>)
                return List.of("(%s)[]".formatted(formatter.formatFirst()));
            return List.of(formatter.formatFirst() + "[]");
        }

        @Override
        public String formatAdapted(Function<IFormatter, String> formatterMethod) {
            if (formatter instanceof FormatterType.Joint<?>)
                return "(%s)[]".formatted(formatterMethod.apply(formatter));
            return formatterMethod.apply(formatter) + "[]";
        }
    }

    public static class JSObject extends FormatterType<PropertyType.JSObject> {
        private final Map<PropertyType.JSObjectKey, FormatterType<?>> keyValues = new HashMap<>();

        public JSObject(PropertyType.JSObject document) {
            super(document);
            document.getKeyValues()
                    .forEach((key, value) -> keyValues.put(
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
                PropertyType.JSObjectKey key = pair.getKey();
                FormatterType<?> value = pair.getValue();
                return "%s: %s".formatted(
                        key.format(),
                        value.formatFirst());
            }).collect(Collectors.joining(", "))));
        }

        @Override
        public String formatAdapted(Function<IFormatter, String> formatterMethod) {
            return "{%s}".formatted(keyValues.entrySet().stream().map(pair -> {
                PropertyType.JSObjectKey key = pair.getKey();
                FormatterType<?> value = pair.getValue();
                return "%s: %s".formatted(
                        key.format(),
                        formatterMethod.apply(value));
            }).collect(Collectors.joining(", ")));
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

        @Override
        public String formatAdapted(Function<IFormatter, String> formatterMethod) {
            return "[%s]".formatted(types.stream().map(formatterMethod).collect(Collectors.joining(", ")));
        }
    }

    public static class TypeOf extends FormatterType<PropertyType.TypeOf> {

        private final FormatterType<?> formatter;

        public TypeOf(PropertyType.TypeOf type) {
            super(type);
            formatter = Serde.getTypeFormatter(type.getComponent());
        }

        @Override
        public TypeOf underscored(boolean underscored) {
            return this;
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of("typeof " + formatter.formatFirst());
        }
    }

    public static class JSLambda extends FormatterType<PropertyType.JSLambda> {
        private final List<IFormatter> params = new ArrayList<>();
        private final FormatterType<?> returns;

        public JSLambda(PropertyType.JSLambda document) {
            super(document);
            document.getParams().forEach(pair -> params.add(
                    (indent, step) -> List.of("%s: %s".formatted(
                            pair.getFirst(),
                            Serde.getTypeFormatter(pair.getSecond()).formatFirst()
                    ))));
            returns = Serde.getTypeFormatter(document.getReturns());
        }

        @Override
        protected List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of("(%s) => %s".formatted(
                    params.stream().map(IFormatter::formatFirst).collect(Collectors.joining(", ")),
                    returns.formatFirst()
            ));
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
        addFormatter(PropertyType.TypeOf.class, TypeOf::new);
        addFormatter(PropertyType.JSLambda.class, JSLambda::new);
    }
}
