package com.probejs.jdoc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.info.type.*;
import com.probejs.jdoc.document.AbstractDocument;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentField;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.property.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.function.Supplier;

public class Serde {
    public static void init() {
        //Types
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyType.Array.class, "type:array");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyType.Clazz.class, "type:class");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyType.Parameterized.class, "type:parameterized");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyType.Variable.class, "type:variable");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyType.Primitive.class, "type:primitive");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyType.Intersection.class, "type:intersection");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyType.Union.class, "type:union");

        //Properties
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyComment.class, "property:comment");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyHide.class, "property:hide");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyMod.class, "property:mod");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyModify.class, "property:modify");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyReturns.class, "property:returns");
        AbstractProperty.PROPERTY_TYPE_REGISTRY.put(PropertyParam.class, "property:param");

        //Documents
        AbstractDocument.DOCUMENT_TYPE_REGISTRY.put(DocumentClass.class, "document:class");
        AbstractDocument.DOCUMENT_TYPE_REGISTRY.put(DocumentMethod.class, "document:method");
        AbstractDocument.DOCUMENT_TYPE_REGISTRY.put(DocumentField.class, "document:field");

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

    public static AbstractProperty deserializeProperty(JsonObject obj) {
        String type = obj.get("type").getAsString();
        try {
            AbstractProperty property = AbstractProperty.PROPERTY_TYPE_REGISTRY.inverse().get(type).getDeclaredConstructor().newInstance();
            property.deserialize(obj);
            return property;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PropertyType<?> constructType(Supplier<PropertyType<?>> builder, ITypeInfo type) {
        PropertyType<?> property = builder.get();
        property.deserializeFromType(type);
        return property;
    }

    public static PropertyType<?> deserializeFromJavaType(ITypeInfo type) {
        if (type instanceof TypeInfoClass) {
            return constructType(PropertyType.Clazz::new, type);
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
    public static <T extends AbstractDocument<T>> void deserializeDocuments(Collection<T> serdes, JsonElement jsonArray) {
        if (jsonArray == null)
            return;
        for (JsonElement element : jsonArray.getAsJsonArray()) {
            T document = (T) deserializeDocument(element.getAsJsonObject());
            if (document != null && document.allModsLoaded())
                serdes.add(document);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractProperty> void deserializeProperties(Collection<T> serdes, JsonElement jsonArray) {
        if (jsonArray == null)
            return;
        for (JsonElement element : jsonArray.getAsJsonArray()) {
            serdes.add((T) deserializeProperty(element.getAsJsonObject()));
        }
    }
}
