package com.probejs.jdoc.property;

import com.google.gson.JsonObject;
import com.probejs.formatter.NameResolver;
import com.probejs.info.type.*;
import com.probejs.jdoc.Serde;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PropertyType extends AbstractProperty {

    public abstract String getTypeName();

    public abstract boolean deserializeFromType(ITypeInfo type);

    public abstract boolean equalsToJavaType(ITypeInfo type);

    public static abstract class Named extends PropertyType {
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
        public boolean deserializeFromType(ITypeInfo type) {
            if (type instanceof TypeInfoClass clazz) {
                name = clazz.getTypeName();
                return true;
            }
            return false;
        }
    }

    public static class Variable extends Named {
        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return type instanceof TypeInfoVariable variable && variable.getTypeName().equals(getName());
        }

        @Override
        public boolean deserializeFromType(ITypeInfo type) {
            if (type instanceof TypeInfoVariable variable) {
                name = variable.getTypeName();
                return true;
            }
            return false;
        }

    }

    public static class Primitive extends Named {
        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return false;
        }

        @Override
        public boolean deserializeFromType(ITypeInfo type) {
            return false;
        }
    }

    public static class Parameterized extends PropertyType {
        protected List<PropertyType> params = new ArrayList<>();
        protected PropertyType base;

        @Override
        public JsonObject serialize() {
            JsonObject object = super.serialize();
            object.add("params", Serde.serializeCollection(params));
            object.add("base", base.serialize());
            return object;
        }

        @Override
        public void deserialize(JsonObject object) {
            Serde.deserializeProperties(params, object.get("params"));
            base = (PropertyType) Serde.deserializeProperty(object.get("base").getAsJsonObject());
        }

        public List<PropertyType> getParams() {
            return params;
        }

        public PropertyType getBase() {
            return base;
        }

        @Override
        public String getTypeName() {
            return base.getTypeName() + "<%s>".formatted(params.stream().map(PropertyType::getTypeName).collect(Collectors.joining(", ")));
        }

        @Override
        public boolean deserializeFromType(ITypeInfo type) {
            if (type instanceof TypeInfoParameterized paramType) {
                base = Serde.deserializeFromJavaType(paramType.getBaseType());
                paramType.getParamTypes().forEach(t -> params.add(Serde.deserializeFromJavaType(t)));
                return true;
            }
            return false;
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
    }

    public static abstract class Joint extends PropertyType {
        protected List<PropertyType> types = new ArrayList<>();

        @Override
        public void deserialize(JsonObject object) {
            Serde.deserializeProperties(types, object.get("types"));
        }

        @Override
        public JsonObject serialize() {
            JsonObject object = super.serialize();
            object.add("types", Serde.serializeCollection(types));
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
        public boolean deserializeFromType(ITypeInfo type) {
            return false;
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

    public static class Array extends PropertyType {
        private PropertyType component;

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
            component = (PropertyType) Serde.deserializeProperty(object.get("component").getAsJsonObject());
        }

        @Override
        public boolean deserializeFromType(ITypeInfo type) {
            if (type instanceof TypeInfoArray array) {
                component = Serde.deserializeFromJavaType(array.getBaseType());
                return true;
            }
            return false;
        }

        @Override
        public boolean equalsToJavaType(ITypeInfo type) {
            return (type instanceof TypeInfoArray array && component.equalsToJavaType(array.getBaseType()));
        }
    }

}
