package com.probejs.docs.formatter.formatter.jdoc;

import com.probejs.ProbeJS;
import com.probejs.docs.formatter.formatter.IFormatter;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.jdoc.property.PropertyValue;
import dev.latvian.mods.rhino.ScriptableObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class FormatterValue<T extends PropertyValue<T, J>, J> extends DocumentFormatter<T> implements IFormatter {
    public static Map<Class<?>, Function<PropertyValue<?, ?>, FormatterValue<?, ?>>> VALUE_FORMATTERS_REGISTRY = new HashMap<>();

    public FormatterValue(T document) {
        super(document);
    }

    @Override
    public boolean hasComment() {
        return false;
    }

    @Override
    public boolean canHide() {
        return false;
    }

    public static class PrimitiveFormatter<T extends PropertyValue<T, J>, J> extends FormatterValue<T, J> {

        public PrimitiveFormatter(T document) {
            super(document);
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            if (document.getValue() instanceof Float floatValue) {
                // return number if it's NaN or Infinity
                if (Float.isNaN(floatValue) || Float.isInfinite(floatValue))
                    return List.of("number");
            }
            if (document.getValue() instanceof Double doubleValue) {
                // return number if it's NaN or Infinity
                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue))
                    return List.of("number");
            }
            return List.of(ProbeJS.GSON.toJson(document.getValue()));
        }
    }

    public static class FallbackFormatter extends FormatterValue<PropertyValue.FallbackValue, Object> {

        public FallbackFormatter(PropertyValue.FallbackValue document) {
            super(document);
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            PropertyType<?> type = document.getType();
            FormatterType<?> typeFormatter = Serde.getTypeFormatter(type);
            return List.of(typeFormatter.formatFirst());
        }

        public PropertyType<?> getType() {
            return document.getType();
        }
    }

    public static class MapFormatter extends FormatterValue<PropertyValue.MapValue, Map<?, ?>> {

        public MapFormatter(PropertyValue.MapValue document) {
            super(document);
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of("{%s}".formatted(document.getValue().entrySet().stream().map(entry -> {
                FormatterValue<?, ?> keyValue = Serde.getValueFormatter(Serde.getValueProperty(entry.getKey()));
                FormatterValue<?, ?> propertyValue = Serde.getValueFormatter(Serde.getValueProperty(entry.getValue()));
                if (keyValue != null && propertyValue != null)
                    return ((keyValue instanceof FallbackFormatter ? "[key: %s]: %s" : "%s: %s").formatted(keyValue.formatFirst(), propertyValue.formatFirst()));
                return null;
            }).filter(Objects::nonNull).collect(Collectors.joining(", "))));
        }
    }

    public static class ListFormatter extends FormatterValue<PropertyValue.ListValue, List<?>> {

        public ListFormatter(PropertyValue.ListValue document) {
            super(document);
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(
                    "[%s]".formatted(document.getValue()
                            .stream()
                            .map(Serde::getValueProperty)
                            .map(Serde::getValueFormatter)
                            .filter(Objects::nonNull)
                            .map(IFormatter::formatFirst)
                            .collect(Collectors.joining(", "))
                    ));
        }
    }

    public static class ObjectFormatter extends FormatterValue<PropertyValue.ObjectValue, ScriptableObject> {

        public ObjectFormatter(PropertyValue.ObjectValue document) {
            super(document);
        }

        @Override
        protected List<String> formatDocument(Integer indent, Integer stepIndent) {
            if (document.getTypeName() == null)
                return List.of(document.getTypeName());
            return List.of("{%s}".formatted(document.getKeyValues().entrySet().stream().map(entry -> {
                FormatterValue<?, ?> key = Serde.getValueFormatter(entry.getKey());
                FormatterValue<?, ?> value = Serde.getValueFormatter(entry.getValue());
                if (key != null && value != null)
                    return "%s: %s".formatted(key.formatFirst(), value.formatFirst());
                return null;
            }).filter(Objects::nonNull).collect(Collectors.joining(", "))));
        }
    }

    public static class NullFormatter extends FormatterValue<PropertyValue.NullValue, Object> {

        public NullFormatter(PropertyValue.NullValue document) {
            super(document);
        }

        @Override
        protected List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of("null");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends PropertyValue<T, J>, J> void addValueFormatter(Class<T> clazz, Function<T, FormatterValue<T, J>> constructor) {
        VALUE_FORMATTERS_REGISTRY.put(clazz, (value) -> constructor.apply((T) value));
    }

    public static void init() {
        addValueFormatter(PropertyValue.NumberValue.class, PrimitiveFormatter::new);
        addValueFormatter(PropertyValue.BooleanValue.class, PrimitiveFormatter::new);
        addValueFormatter(PropertyValue.StringValue.class, PrimitiveFormatter::new);
        addValueFormatter(PropertyValue.CharacterValue.class, PrimitiveFormatter::new);
        addValueFormatter(PropertyValue.FallbackValue.class, FallbackFormatter::new);
        addValueFormatter(PropertyValue.MapValue.class, MapFormatter::new);
        addValueFormatter(PropertyValue.ListValue.class, ListFormatter::new);
        addValueFormatter(PropertyValue.ObjectValue.class, ObjectFormatter::new);
        addValueFormatter(PropertyValue.NullValue.class, NullFormatter::new);
    }
}
