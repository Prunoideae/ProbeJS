package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.jdoc.JsAnnotations;
import com.probejs.jdoc.java.MethodInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyModify;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.jdoc.property.PropertyReturns;
import com.probejs.jdoc.property.PropertyType;
import dev.latvian.mods.kubejs.typings.JsInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocumentMethod extends AbstractDocument<DocumentMethod> {
    public String name;
    public boolean isStatic;
    public boolean isAbstract;

    public PropertyType<?> returns;

    public final List<PropertyParam> params = new ArrayList<>();
    public final List<PropertyType<?>> variables = new ArrayList<>();

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("name", name);
        object.addProperty("static", isStatic);
        object.addProperty("abstract", isAbstract);
        Serde.serializeCollection(object, "params", params);
        Serde.serializeCollection(object, "variables", variables);
        object.add("returns", returns.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        name = object.get("name").getAsString();
        if (object.has("static"))
            isStatic = object.get("static").getAsBoolean();
        if (object.has("abstract"))
            isAbstract = object.get("abstract").getAsBoolean();
        Serde.deserializeDocuments(params, object.get("params"));
        Serde.deserializeDocuments(variables, object.get("variables"));
        returns = (PropertyType<?>) Serde.deserializeProperty(object.get("returns").getAsJsonObject());
    }

    public static DocumentMethod fromJava(MethodInfo info) {
        DocumentMethod document = new DocumentMethod();
        document.name = info.getName();
        document.returns = Serde.deserializeFromJavaType(info.getReturnType());
        document.isStatic = info.isStatic();
        document.isAbstract = info.isAbstract();
        info.getParams().stream()
                .map(PropertyParam::fromJava)
                .forEach(document.params::add);
        info.getAnnotations().stream().filter(annotation -> annotation instanceof Deprecated).findFirst().ifPresent(annotation -> {
            document.builtinComments.add("@deprecated");
            if (((Deprecated) annotation).forRemoval()) {
                document.builtinComments.add("This method is marked to be removed in future!");
            }
        });

        info.getAnnotations().stream().filter(annotation -> annotation instanceof JsInfo).findFirst().ifPresent(annotation -> {
            document.builtinComments.merge(JsAnnotations.fromAnnotation((JsInfo) annotation, true));
        });

        info.getTypeVariables().stream()
                .map(Serde::deserializeFromJavaType)
                .forEach(document.variables::add);
        return document;
    }

    @Override
    public DocumentMethod applyProperties() {
        DocumentMethod copy = copy();
        copy.findPropertiesOf(PropertyModify.class).forEach(modify -> {
            PropertyParam param = copy.params.get(modify.getOrdinal());
            copy.params.set(modify.getOrdinal(), new PropertyParam(
                    modify.getName() != null ? modify.getName() : param.getName(),
                    modify.getNewType() != null ? modify.getNewType() : param.getType(),
                    param.isVarArg()
            ));
        });
        copy.findProperty(PropertyReturns.class).ifPresent(propertyReturns -> copy.returns = propertyReturns.getType());
        return copy;
    }

    @Override
    public DocumentMethod copy() {
        DocumentMethod document = new DocumentMethod();
        document.name = name;
        document.params.addAll(params);
        document.returns = returns;
        document.properties.addAll(properties);
        document.variables.addAll(variables);
        document.isStatic = isStatic;
        document.isAbstract = isAbstract;
        return document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentMethod that = (DocumentMethod) o;
        return Objects.equals(name, that.name) && Objects.equals(params, that.params) && Objects.equals(returns, that.returns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, params, returns);
    }

    public String getName() {
        return name;
    }

    public List<PropertyParam> getParams() {
        return params;
    }

    public PropertyType<?> getReturns() {
        return returns;
    }

    public List<PropertyType<?>> getVariables() {
        return variables;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }
}
