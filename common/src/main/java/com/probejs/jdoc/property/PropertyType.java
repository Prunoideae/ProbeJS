package com.probejs.jdoc.property;

import com.google.gson.JsonObject;
import com.probejs.formatter.NameResolver;
import com.probejs.info.type.*;
import com.probejs.jdoc.Serde;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class PropertyType<T extends PropertyType<T>> extends AbstractProperty {

    public abstract String getTypeName();

    public abstract void deserializeFromType(ITypeInfo type);

    public abstract boolean equalsToJavaType(ITypeInfo type);

    public abstract boolean typeEquals(T type);

    public static abstract class Named extends PropertyType<Named> {
        protected String name;

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
        public boolean typeEquals(Named type) {
            return name.equals(type.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    public static class Clazz extends Named {

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
        public void deserializeFromType(ITypeInfo type) {
            if (type instanceof TypeInfoClass clazz) {
                name = clazz.getTypeName();
            }
        }
    }

    public static class Variable extends Named {
        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return type instanceof TypeInfoVariable variable && variable.getTypeName().equals(getName());
        }

        @Override
        public void deserializeFromType(ITypeInfo type) {
            if (type instanceof TypeInfoVariable variable) {
                name = variable.getTypeName();
            }
        }

    }

    public static class Primitive extends Named {
        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return false;
        }

        @Override
        public void deserializeFromType(ITypeInfo type) {
        }
    }

    public static class Parameterized extends PropertyType<Parameterized> {
        protected List<PropertyType<?>> params = new ArrayList<>();
        protected PropertyType<?> base;

        @Override
        public JsonObject serialize() {
            JsonObject object = super.serialize();
            Serde.serializeCollection(object,"params", params);
            object.add("base", base.serialize());
            return object;
        }

        @Override
        public void deserialize(JsonObject object) {
            Serde.deserializeProperties(params, object.get("params"));
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
        public void deserializeFromType(ITypeInfo type) {
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
    }

    public static abstract class Joint extends PropertyType<Joint> {
        protected List<PropertyType<?>> types = new ArrayList<>();

        @Override
        public void deserialize(JsonObject object) {
            Serde.deserializeProperties(types, object.get("types"));
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
            return types.stream().map(PropertyType::getTypeName).map("(%s)"::formatted).collect(Collectors.joining(getDelimiter()));
        }

        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return false;
        }

        @Override
        public void deserializeFromType(ITypeInfo type) {
        }

        @Override
        public boolean typeEquals(Joint type) {
            return types.equals(type.types);
        }

        @Override
        public int hashCode() {
            return Objects.hash(types);
        }
    }

    public static class Intersection extends Joint {
        @Override
        public String getDelimiter() {
            return "&";
        }
    }

    public static class Union extends Joint {
        @Override
        public String getDelimiter() {
            return "|";
        }
    }

    public static class Array extends PropertyType<Array> {
        private PropertyType<?> component;

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
        public void deserializeFromType(ITypeInfo type) {
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
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return this.getClass() == obj.getClass() && this.typeEquals((T) obj);
    }
}
