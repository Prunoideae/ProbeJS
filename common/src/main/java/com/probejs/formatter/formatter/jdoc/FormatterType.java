package com.probejs.formatter.formatter.jdoc;

import com.probejs.formatter.NameResolver;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.jdoc.property.PropertyType;

import java.util.List;
import java.util.stream.Collectors;

public abstract class FormatterType<T extends PropertyType<T>> extends DocumentFormatter<T> {
    protected boolean underscored;

    public FormatterType(T document) {
        super(document);
    }


    public void setUnderscored(boolean underscored) {
        this.underscored = underscored;
    }

    public static abstract class Named<T extends PropertyType.Named<T>> extends FormatterType<T> {
        public Named(T type) {
            super(type);
        }

        @Override
        public List<String> format(Integer indent, Integer stepIndent) {
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
        public List<String> format(Integer indent, Integer stepIndent) {
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
        public List<String> format(Integer indent, Integer stepIndent) {
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
        public List<String> format(Integer indent, Integer stepIndent) {
            return List.of(formatter.formatFirst() + "[]");
        }
    }

    public static FormatterType<?> getFormatter(PropertyType<?> type) {
        if (type instanceof PropertyType.Clazz clazz) {
            return new Clazz(clazz);
        }
        if (type instanceof PropertyType.Variable variable) {
            return new Variable(variable);
        }
        if (type instanceof PropertyType.Native nativeType) {
            return new Native(nativeType);
        }
        if (type instanceof PropertyType.Parameterized parameterized) {
            return new Parameterized(parameterized);
        }
        if (type instanceof PropertyType.Intersection intersection) {
            return new Intersection(intersection);
        }
        if (type instanceof PropertyType.Union union) {
            return new Union(union);
        }
        if (type instanceof PropertyType.Array array) {
            return new Array(array);
        }
        return new Native(new PropertyType.Native("any"));
    }
}
