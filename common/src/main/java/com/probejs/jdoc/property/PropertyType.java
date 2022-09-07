package com.probejs.jdoc.property;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.ProbeJS;
import com.probejs.formatter.NameResolver;
import com.probejs.info.type.*;
import com.probejs.jdoc.Serde;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class PropertyType<T extends PropertyType<T>> extends AbstractProperty<T> {

    public static PropertyType<?> wrapNonNull(PropertyType<?> inner) {
        return new Parameterized(new Native("NonNullable"), List.of(inner));
    }

    public abstract String getTypeName();

    public abstract void fromJava(ITypeInfo type);

    public abstract boolean equalsToJavaType(ITypeInfo type);

    public abstract boolean typeEquals(T type);

    public static abstract class Named<T extends Named<T>> extends PropertyType<T> {
        protected String name;

        public Named(String name) {
            this.name = name;
        }

        public Named() {
        }

        @Override
        public JsonObject serialize() {
            JsonObject object = super.serialize();
            object.addProperty("name", name);
            return object;
        }

        @Override
        public void deserialize(JsonObject object) {
            name = object.get("name").getAsString();
        }

        public String getName() {
            return name;
        }

        @Override
        public String getTypeName() {
            return getName();
        }

        @Override
        public boolean typeEquals(T type) {
            return name.equals(type.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    public static class Clazz extends Named<Clazz> {
        public Clazz(String name) {
            super(name);
        }

        public Clazz() {
        }

        public String getClassName() {
            return name;
        }

        /**
         * @return The class, null if the class can't be found.
         */
        @Nullable
        public Class<?> getDocumentClass() {
            try {
                return Class.forName(this.name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        @Override
        public String getTypeName() {
            return NameResolver.getResolvedName(getClassName()).getFullName();
        }

        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            if (type instanceof TypeInfoWildcard && name.equals("java.lang.Object"))
                return true;
            return type instanceof TypeInfoClass clazz && clazz.getTypeName().equals(getClassName());
        }

        @Override
        public void fromJava(ITypeInfo type) {
            if (type instanceof TypeInfoClass clazz) {
                name = clazz.getTypeName();
            }
        }

        @Override
        public Clazz copy() {
            return new Clazz(name);
        }
    }

    public static class Variable extends Named<Variable> {
        public Variable(String name) {
            super(name);
        }

        public Variable() {
        }

        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return type instanceof TypeInfoVariable variable && variable.getTypeName().equals(getName());
        }

        @Override
        public void fromJava(ITypeInfo type) {
            if (type instanceof TypeInfoVariable variable) {
                name = variable.getTypeName();
            }
        }

        @Override
        public Variable copy() {
            return new Variable(name);
        }
    }

    public static class Native extends Named<Native> {
        public Native(String name) {
            super(name);
        }

        public Native() {
        }

        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return false;
        }

        @Override
        public void fromJava(ITypeInfo type) {
        }

        @Override
        public Native copy() {
            return new Native(name);
        }
    }

    public static class Parameterized extends PropertyType<Parameterized> {
        protected List<PropertyType<?>> params = new ArrayList<>();
        protected PropertyType<?> base;

        public Parameterized(PropertyType<?> base, List<PropertyType<?>> params) {
            this.base = base;
            this.params = params;
        }

        public Parameterized() {
        }

        @Override
        public JsonObject serialize() {
            JsonObject object = super.serialize();
            Serde.serializeCollection(object, "params", params);
            object.add("base", base.serialize());
            return object;
        }

        @Override
        public void deserialize(JsonObject object) {
            Serde.deserializeDocuments(params, object.get("params"));
            base = (PropertyType<?>) Serde.deserializeProperty(object.get("base").getAsJsonObject());
        }

        public List<PropertyType<?>> getParams() {
            return params;
        }

        public PropertyType<?> getBase() {
            return base;
        }

        @Override
        public String getTypeName() {
            return base.getTypeName() + "<%s>".formatted(params.stream().map(PropertyType::getTypeName).collect(Collectors.joining(", ")));
        }

        @Override
        public void fromJava(ITypeInfo type) {
            if (type instanceof TypeInfoParameterized paramType) {
                base = Serde.deserializeFromJavaType(paramType.getBaseType());
                paramType.getParamTypes().forEach(t -> params.add(Serde.deserializeFromJavaType(t)));
            }
        }

        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            if (type instanceof TypeInfoParameterized paramType) {
                if (!base.equalsToJavaType(paramType.getBaseType())) {
                    return false;
                }
                List<ITypeInfo> args = paramType.getParamTypes();
                if (params.size() != args.size())
                    return false;
                for (int i = 0; i < args.size(); i++) {
                    if (!params.get(i).equalsToJavaType(args.get(i)))
                        return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean typeEquals(Parameterized type) {
            return base.equals(type.base) && params.equals(type.params);
        }

        @Override
        public int hashCode() {
            return Objects.hash(params, base);
        }

        @Override
        public Parameterized copy() {
            return new Parameterized(base, params);
        }
    }

    public static abstract class Joint<T extends Joint<T>> extends PropertyType<T> {
        protected List<PropertyType<?>> types = new ArrayList<>();

        public Joint() {
        }

        public Joint(List<PropertyType<?>> types) {
            this.types = types;
        }

        @Override
        public void deserialize(JsonObject object) {
            Serde.deserializeDocuments(types, object.get("types"));
        }

        @Override
        public JsonObject serialize() {
            JsonObject object = super.serialize();
            Serde.serializeCollection(object, "types", types);
            return object;
        }

        public abstract String getDelimiter();

        @Override
        public String getTypeName() {
            return types.stream()
                    .map(prop -> ((prop instanceof PropertyType.Joint<?>) ? "(%s)" : "%s").formatted(prop.getTypeName())).map("(%s)"::formatted)
                    .collect(Collectors.joining(getDelimiter()));
        }

        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return false;
        }

        @Override
        public void fromJava(ITypeInfo type) {
        }

        @Override
        public boolean typeEquals(T type) {
            return types.equals(type.types);
        }

        @Override
        public int hashCode() {
            return Objects.hash(types);
        }

        public List<PropertyType<?>> getTypes() {
            return types;
        }
    }

    public static class Intersection extends Joint<Intersection> {
        public Intersection() {
        }

        public Intersection(List<PropertyType<?>> types) {
            super(types);
        }

        @Override
        public String getDelimiter() {
            return "&";
        }

        @Override
        public Intersection copy() {
            return new Intersection(types);
        }
    }

    public static class Union extends Joint<Union> {
        public Union() {
        }

        public Union(List<PropertyType<?>> types) {
            super(types);
        }

        @Override
        public String getDelimiter() {
            return "|";
        }

        @Override
        public Union copy() {
            return new Union(types);
        }
    }

    public static class Array extends PropertyType<Array> {
        private PropertyType<?> component;

        public Array(PropertyType<?> component) {
            this.component = component;
        }

        public Array() {

        }

        @Override
        public String getTypeName() {
            return component.getTypeName() + "[]";
        }

        @Override
        public JsonObject serialize() {
            JsonObject object = super.serialize();
            object.add("component", component.serialize());
            return object;
        }

        @Override
        public void deserialize(JsonObject object) {
            component = (PropertyType<?>) Serde.deserializeProperty(object.get("component").getAsJsonObject());
        }

        @Override
        public void fromJava(ITypeInfo type) {
            if (type instanceof TypeInfoArray array) {
                component = Serde.deserializeFromJavaType(array.getBaseType());
            }
        }

        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return (type instanceof TypeInfoArray array && component.equalsToJavaType(array.getBaseType()));
        }

        @Override
        public boolean typeEquals(Array type) {
            return component.equals(type.component);
        }

        @Override
        public int hashCode() {
            return Objects.hash(component);
        }

        @Override
        public Array copy() {
            return new Array(component);
        }

        public PropertyType<?> getComponent() {
            return component;
        }
    }

    public static class JSObject extends PropertyType<JSObject> {

        private final Map<Object, PropertyType<?>> keyValues = new HashMap<>();

        public JSObject() {

        }

        public JSObject(Map<Object, PropertyType<?>> keyValues) {
            this.keyValues.putAll(keyValues);
        }


        @Override
        public JSObject copy() {
            return new JSObject(keyValues);
        }

        @Override
        public JsonObject serialize() {
            JsonObject object = super.serialize();
            JsonArray keyValuePairs = new JsonArray();
            for (Map.Entry<Object, PropertyType<?>> entry : keyValues.entrySet()) {
                Object key = entry.getKey();
                PropertyType<?> value = entry.getValue();
                JsonObject pair = new JsonObject();
                if (key instanceof PropertyType<?> propertyType) {
                    pair.add("key", propertyType.serialize());
                } else {
                    pair.add("key", Serde.getPrimitive(key));
                }
                pair.add("value", value.serialize());
                keyValuePairs.add(pair);
            }
            object.add("members", keyValuePairs);
            return object;
        }

        @Override
        public void deserialize(JsonObject object) {
            for (JsonElement element : object.get("members").getAsJsonArray()) {
                JsonObject keyValuePair = element.getAsJsonObject();
                JsonElement keyJson = keyValuePair.get("key");
                Object key = keyJson.isJsonObject() ? Serde.deserializeProperty(keyJson.getAsJsonObject()) : Serde.getAsPrimitive(keyJson);
                keyValues.put(key, (PropertyType<?>) Serde.deserializeProperty(keyValuePair.get("value").getAsJsonObject()));
            }
        }

        @Override
        public String getTypeName() {
            return "{%s}".formatted(keyValues.entrySet().stream().map(entry -> {
                Object key = entry.getKey();
                PropertyType<?> value = entry.getValue();
                return "%s: %s".formatted((
                        key instanceof PropertyType<?> propertyType ?
                                "[key: (%s) & string]".formatted(propertyType.getTypeName()) :
                                ProbeJS.GSON.toJson(key)), value.getTypeName());
            }).collect(Collectors.joining(", ")));
        }

        @Override
        public void fromJava(ITypeInfo type) {

        }

        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return false;
        }

        @Override
        public boolean typeEquals(JSObject type) {
            return keyValues.equals(type.keyValues);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyValues);
        }

        public Map<Object, PropertyType<?>> getKeyValues() {
            return keyValues;
        }
    }

    public static class JSArray extends PropertyType<JSArray> {
        private final List<PropertyType<?>> types = new ArrayList<>();

        public JSArray() {

        }

        public JSArray(List<PropertyType<?>> types) {
            this.types.addAll(types);
        }

        @Override
        public JSArray copy() {
            return new JSArray(types);
        }

        @Override
        public JsonObject serialize() {
            JsonObject object = super.serialize();
            Serde.serializeCollection(object, "types", types);
            return object;
        }

        @Override
        public void deserialize(JsonObject object) {
            Serde.deserializeDocuments(types, object.get("types"));
        }

        @Override
        public String getTypeName() {
            return "[%s]".formatted(types.stream().map(PropertyType::getTypeName).collect(Collectors.joining(", ")));
        }

        @Override
        public void fromJava(ITypeInfo type) {

        }

        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return false;
        }

        @Override
        public boolean typeEquals(JSArray type) {
            return types.equals(type.types);
        }

        @Override
        public int hashCode() {
            return Objects.hash(types);
        }

        public List<PropertyType<?>> getTypes() {
            return types;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean equals(Object obj) {
        if (obj == null)
            return false;
        return this.getClass() == obj.getClass() && this.typeEquals((T) obj);
    }

    @Override
    public String toString() {
        return getTypeName();
    }
}
