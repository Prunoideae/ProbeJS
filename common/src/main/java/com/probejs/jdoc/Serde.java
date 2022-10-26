package com.probejs.jdoc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.probejs.formatter.formatter.jdoc.FormatterType;
import com.probejs.formatter.formatter.jdoc.FormatterValue;
import com.probejs.info.type.*;
import com.probejs.jdoc.document.*;
import com.probejs.jdoc.property.*;
import com.probejs.jdoc.property.condition.PropertyMod;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Serde {
    public static void init() {
        //Types
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyType.Array.class, "type:array");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyType.Clazz.class, "type:class");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyType.Parameterized.class, "type:parameterized");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyType.Variable.class, "type:variable");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyType.Native.class, "type:primitive");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyType.Intersection.class, "type:intersection");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyType.Union.class, "type:union");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyType.JSObject.class, "type:object");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyType.JSArray.class, "type:jsArray");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyType.TypeOf.class, "type:typeof");

        //Properties
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyComment.class, "property:comment");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyHide.class, "property:hide");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyMod.class, "property:mod");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyModify.class, "property:modify");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyReturns.class, "property:returns");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyParam.class, "property:param");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyAssign.class, "property:assign");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyUnderscored.class, "property:underscored");

        //Documents
        AbstractDocument.DOCUMENT_TYPE_REGISTRY.put(DocumentClass.class, "document:class");
        AbstractDocument.DOCUMENT_TYPE_REGISTRY.put(DocumentMethod.class, "document:method");
        AbstractDocument.DOCUMENT_TYPE_REGISTRY.put(DocumentField.class, "document:field");
        AbstractDocument.DOCUMENT_TYPE_REGISTRY.put(DocumentConstructor.class, "document:constructor");

        //Values
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyValue.NumberValue.class, "value:number");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyValue.BooleanValue.class, "value:boolean");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyValue.StringValue.class, "value:string");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyValue.CharacterValue.class, "value:character");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyValue.FallbackValue.class, "value:fallback");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyValue.NullValue.class, "value:null");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyValue.MapValue.class, "value:map");
        AbstractProperty.DOCUMENT_TYPE_REGISTRY.put(PropertyValue.ListValue.class, "value:list");

        //We separate property holder and formatter here since we want to make a language-independent backend
        PropertyValue.init();
        FormatterValue.init();
        FormatterType.init();
    }

    public static AbstractDocument<?> deserializeDocument(JsonObject obj) {
        String type = obj.get("type").getAsString();
        try {
            AbstractDocument<?> doc = AbstractDocument.DOCUMENT_TYPE_REGISTRY.inverse().get(type).getDeclaredConstructor().newInstance();
            doc.deserialize(obj);
            return doc;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AbstractProperty<?> deserializeProperty(JsonObject obj) {
        String type = obj.get("type").getAsString();
        try {
            AbstractProperty<?> property = (AbstractProperty<?>) AbstractProperty.DOCUMENT_TYPE_REGISTRY.inverse().get(type).getDeclaredConstructor().newInstance();
            property.deserialize(obj);
            return property;
        } catch (InstantiationException | IllegalAccessException
                 | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PropertyType<?> constructType(Supplier<PropertyType<?>> builder, ITypeInfo type) {
        PropertyType<?> property = builder.get();
        property.fromJava(type);
        return property;
    }

    public static PropertyType<?> deserializeFromJavaType(ITypeInfo type, boolean insideType) {
        if (type instanceof TypeInfoClass clazz) {
            if (!clazz.getTypeVariables().isEmpty() && !insideType)
                return constructType(PropertyType.Parameterized::new, clazz);
            else
                return constructType(PropertyType.Clazz::new, clazz);
        }
        if (type instanceof TypeInfoArray) {
            return constructType(PropertyType.Array::new, type);
        }
        if (type instanceof TypeInfoVariable) {
            return constructType(PropertyType.Variable::new, type);
        }
        if (type instanceof TypeInfoParameterized) {
            return constructType(PropertyType.Parameterized::new, type);
        }
        if (type instanceof TypeInfoWildcard) {
            return constructType(PropertyType.Clazz::new, new TypeInfoClass(Object.class));
        }
        return null;
    }

    public static PropertyType<?> deserializeFromJavaType(ITypeInfo type) {
        return deserializeFromJavaType(type, false);
    }


    public static void serializeCollection(JsonObject object, String key, Iterable<? extends ISerde> serdes) {
        serializeCollection(object, key, serdes, false);
    }

    public static void serializeCollection(JsonObject object, String key, Iterable<? extends ISerde> serdes, boolean skipIfEmpty) {
        if (serdes == null)
            return;
        JsonArray result = new JsonArray();
        serdes.forEach(serde -> result.add(serde.serialize()));
        if (result.isEmpty() && skipIfEmpty)
            return;
        object.add(key, result);
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractDocument<?>> void deserializeDocuments(Collection<T> serdes, JsonElement jsonArray) {
        if (jsonArray == null || serdes == null)
            return;
        for (JsonElement element : jsonArray.getAsJsonArray()) {
            T document = (T) deserializeDocument(element.getAsJsonObject());
            if (document != null && document.fulfillsConditions())
                serdes.add(document);
        }
    }

    public static PropertyValue<?, ?> getValueProperty(Object o) {
        if (o == null)
            return new PropertyValue.NullValue();
        Function<Object, PropertyValue<?, ?>> constructor = PropertyValue.VALUES_REGISTRY.get(o.getClass());
        if (constructor != null) {
            return constructor.apply(o);
        }
        for (Map.Entry<Class<?>, Function<Object, PropertyValue<?, ?>>> entry : PropertyValue.VALUES_REGISTRY.entrySet()) {
            Class<?> clazz = entry.getKey();
            Function<Object, PropertyValue<?, ?>> subConstructor = entry.getValue();
            if (clazz.isAssignableFrom(o.getClass()))
                return subConstructor.apply(o);
        }
        return new PropertyValue.FallbackValue(o);
    }

    public static FormatterValue<?, ?> getValueFormatter(PropertyValue<?, ?> property) {
        Function<PropertyValue<?, ?>, FormatterValue<?, ?>> constructor = FormatterValue.VALUE_FORMATTERS_REGISTRY.get(property.getClass());
        if (constructor != null)
            return constructor.apply(property);
        return null;
    }

    public static FormatterType<?> getTypeFormatter(PropertyType<?> type) {
        Function<PropertyType<?>, FormatterType<?>> constructor = FormatterType.FORMATTER_REGISTRY.get(type.getClass());
        if (constructor != null)
            return constructor.apply(type);
        return new FormatterType.Native(new PropertyType.Native("any"));
    }

    public static JsonElement getPrimitive(Object o) {
        if (o instanceof Boolean bool)
            return new JsonPrimitive(bool);
        if (o instanceof String string)
            return new JsonPrimitive(string);
        if (o instanceof Number number)
            return new JsonPrimitive(number);
        if (o instanceof Character character)
            return new JsonPrimitive(character);
        throw new IllegalArgumentException("The argument is not primitive type!");
    }

    public static Object getAsPrimitive(JsonElement element) {
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean())
            return primitive.getAsBoolean();
        if (primitive.isNumber())
            return primitive.getAsNumber();
        if (primitive.isString())
            return primitive.getAsString();
        throw new IllegalArgumentException("The argument is not primitive value!");
    }
}
