package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.info.MethodInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyType;

import java.util.ArrayList;
import java.util.List;

public class DocumentMethod extends AbstractDocument<DocumentMethod> {
    private String name;
    private final List<PropertyType> params = new ArrayList<>();
    private PropertyType returns;


    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("name", name);
        object.add("params", Serde.serializeCollection(params));
        object.add("returns", returns.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        name = object.get("name").getAsString();
        Serde.deserializeProperties(params, object.get("params").getAsJsonArray());
        returns = (PropertyType) Serde.deserializeProperty(object.get("returns").getAsJsonObject());
    }

    public boolean matchMethod(MethodInfo info) {
        if (!info.getName().equals(name))
            return false;
        if (!returns.equalsToJavaType(info.getReturnType()))
            return false;
        List<MethodInfo.ParamInfo> methodTypes = info.getParams();
        if (params.size() != methodTypes.size())
            return false;
        for (int i = 0; i < params.size(); i++) {
            if (!params.get(i).equalsToJavaType(methodTypes.get(i).getType()))
                return false;
        }
        return true;
    }

    public static DocumentMethod fromJava(MethodInfo info) {
        DocumentMethod document = new DocumentMethod();
        document.name = info.getName();
        document.returns = Serde.deserializeFromJavaType(info.getReturnType());
        info.getParams().stream()
                .map(MethodInfo.ParamInfo::getType)
                .map(Serde::deserializeFromJavaType)
                .forEach(document.params::add);
        return document;
    }

    @Override
    public DocumentMethod merge(DocumentMethod other) {
        return other.copy();
    }

    @Override
    public DocumentMethod copy() {
        DocumentMethod document = new DocumentMethod();
        document.name = name;
        document.params.addAll(params);
        document.returns = returns;
        document.properties.addAll(properties);
        return document;
    }
}
