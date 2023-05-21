package com.probejs.jdoc.property;

import com.google.gson.JsonObject;
import com.probejs.jdoc.java.MethodInfo;
import com.probejs.jdoc.Serde;

import java.util.Objects;

public class PropertyParam extends AbstractProperty<PropertyParam> {
    private String name;
    private PropertyType<?> type;
    private boolean varArg;

    public PropertyParam(String name, PropertyType<?> type, boolean isVarArg) {
        this.name = name;
        this.type = type;
        varArg = isVarArg;
    }

    public PropertyParam() {

    }

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("name", name);
        object.add("paramType", type.serialize());
        object.addProperty("varArg", varArg);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        name = object.get("name").getAsString();
        type = (PropertyType<?>) Serde.deserializeProperty(object.get("paramType").getAsJsonObject());
        if (object.has("varArg"))
            varArg = object.get("varArg").getAsBoolean();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyParam that = (PropertyParam) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    public PropertyType<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static PropertyParam fromJava(MethodInfo.ParamInfo info) {
        PropertyParam param = new PropertyParam();
        param.name = info.getName();
        param.type = Serde.deserializeFromJavaType(info.getType());
        param.varArg = info.isVararg();
        return param;

    }

    @Override
    public PropertyParam copy() {
        return new PropertyParam(name, type, varArg);
    }

    public boolean isVarArg() {
        return varArg;
    }
}
