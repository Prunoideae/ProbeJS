package com.probejs.jdoc.property;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.probejs.info.type.TypeInfoClass;
import com.probejs.jdoc.Serde;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class PropertyValue<T> extends AbstractProperty<PropertyValue<T>> {
    public static Map<Class<?>, Function<Object, PropertyValue<?>>> VALUES_REGISTRY = new HashMap<>();
    protected T value;

    public PropertyValue() {

    }

    public PropertyValue(T value) {
        this.value = value;
    }

    public abstract JsonElement serializeValue();

    public abstract T deserializeValue(JsonElement value);

    @Override
    public final JsonObject serialize() {
        JsonObject object = super.serialize();
        JsonElement value = serializeValue();
        if (value != null)
            object.add("value", value);
        return object;
    }

    @Override
    public final void deserialize(JsonObject object) {
        if (object.has("value"))
            value = deserializeValue(object.get("value"));
    }

    @Override
    public PropertyValue<T> copy() {
        return this;
    }

    @SuppressWarnings("unchecked")
    public static <T> void addValueTransformer(Class<T> clazz, Function<T, PropertyValue<T>> constructor) {
        VALUES_REGISTRY.put(clazz, (value) -> constructor.apply((T) value));
    }

    public static class NumberValue extends PropertyValue<Number> {
        public NumberValue() {

        }

        public NumberValue(Number value) {
            super(value);
        }

        @Override
        public JsonElement serializeValue() {
            return new JsonPrimitive(value);
        }

        @Override
        public Integer deserializeValue(JsonElement value) {
            return value.getAsInt();
        }
    }

    public static class BooleanValue extends PropertyValue<Boolean> {
        public BooleanValue() {
        }

        public BooleanValue(Boolean value) {
            super(value);
        }

        @Override
        public JsonElement serializeValue() {
            return new JsonPrimitive(value);
        }

        @Override
        public Boolean deserializeValue(JsonElement value) {
            return value.getAsBoolean();
        }
    }

    public static class StringValue extends PropertyValue<String> {

        public StringValue() {
        }

        public StringValue(String value) {
            super(value);
        }

        @Override
        public JsonElement serializeValue() {
            return new JsonPrimitive(value);
        }

        @Override
        public String deserializeValue(JsonElement value) {
            return value.getAsString();
        }
    }

    public static class CharacterValue extends PropertyValue<Character> {
        public CharacterValue() {
        }

        public CharacterValue(Character value) {
            super(value);
        }

        @Override
        public JsonElement serializeValue() {
            return new JsonPrimitive(value);
        }

        @Override
        public Character deserializeValue(JsonElement value) {
            return value.getAsString().charAt(0);
        }
    }

    public static class FallbackValue extends PropertyValue<Object> {

        public FallbackValue() {
        }

        public FallbackValue(Object value) {
            super(value);
            if (!(value instanceof PropertyType<?>))
                this.value = Serde.deserializeFromJavaType(new TypeInfoClass(value.getClass()));
        }

        public PropertyType<?> getType() {
            return (PropertyType<?>) value;
        }

        @Override
        public JsonElement serializeValue() {
            return getType().serialize();
        }

        @Override
        public Object deserializeValue(JsonElement value) {
            return Serde.deserializeProperty(value.getAsJsonObject());
        }
    }

    public static class MapValue extends PropertyValue<Map<?, ?>> {
        public MapValue() {
        }

        public MapValue(Map<?, ?> value) {
            super(value);
        }

        @Override
        public JsonElement serializeValue() {
            JsonArray array = new JsonArray();
            for (Map.Entry<?, ?> entry : value.entrySet()) {
                PropertyValue<?> keyObj = Serde.getValueProperty(entry.getKey());
                PropertyValue<?> valueObj = Serde.getValueProperty(entry.getValue());
                JsonObject object = new JsonObject();
                object.add("key", keyObj.serialize());
                object.add("value", valueObj.serialize());
                array.add(object);
            }
            return array;
        }

        @Override
        public Map<?, ?> deserializeValue(JsonElement value) {
            HashMap<Object, Object> map = new HashMap<>();
            for (JsonElement element : value.getAsJsonArray()) {
                JsonObject keyValue = element.getAsJsonObject();
                PropertyValue<?> keyProperty = (PropertyValue<?>) Serde.deserializeProperty(keyValue.get("key").getAsJsonObject());
                PropertyValue<?> valueProperty = (PropertyValue<?>) Serde.deserializeProperty(keyValue.get("value").getAsJsonObject());
                if (keyProperty != null && valueProperty != null)
                    map.put(keyProperty.value, valueProperty.value);
            }
            return map;
        }
    }

    public static class ListValue extends PropertyValue<List<?>> {
        public ListValue() {
        }

        public ListValue(List<?> value) {
            super(value);
        }

        @Override
        public JsonElement serializeValue() {
            JsonArray array = new JsonArray();
            value.forEach(v -> array.add(Serde.getValueProperty(v).serialize()));
            return array;
        }

        @Override
        public List<?> deserializeValue(JsonElement value) {
            List<Object> values = new ArrayList<>();
            for (JsonElement element : value.getAsJsonArray()) {
                PropertyValue<?> valueProperty = (PropertyValue<?>) Serde.deserializeProperty(element.getAsJsonObject());
                if (valueProperty != null)
                    values.add(valueProperty.value);
            }
            return values;
        }
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        addValueTransformer(Number.class, NumberValue::new);
        addValueTransformer(Boolean.class, BooleanValue::new);
        addValueTransformer(String.class, StringValue::new);
        addValueTransformer(Character.class, CharacterValue::new);
        addValueTransformer((Class<Map<?, ?>>) ((Class<?>) Map.class), MapValue::new);
        addValueTransformer((Class<List<?>>) ((Class<?>) List.class), ListValue::new);
    }
}
