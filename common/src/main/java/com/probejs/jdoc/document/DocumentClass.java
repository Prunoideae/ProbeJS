package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.info.ClassInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyType;

import java.util.ArrayList;
import java.util.List;

public class DocumentClass extends AbstractDocument<DocumentClass> {
    protected String name;
    protected List<PropertyType> generics;
    protected PropertyType parent;
    protected List<PropertyType> interfaces;
    protected List<DocumentField> fields = new ArrayList<>();
    protected List<DocumentMethod> methods = new ArrayList<>();

    protected boolean isAbstract;
    protected boolean isInterface;


    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("className", name);
        object.addProperty("interface", isInterface);
        object.addProperty("abstract", isAbstract);

        if (parent != null)
            object.add("parent", parent.serialize());
        object.add("fields", Serde.serializeCollection(fields));
        object.add("methods", Serde.serializeCollection(methods));
        object.add("variables", Serde.serializeCollection(generics));
        object.add("interfaces", Serde.serializeCollection(interfaces));
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        name = object.get("className").getAsString();
        isInterface = object.get("interface").getAsBoolean();
        isAbstract = object.get("abstract").getAsBoolean();
        if (object.has("parent"))
            parent = (PropertyType) Serde.deserializeProperty(object.get("parent").getAsJsonObject());
        Serde.deserializeDocuments(this.fields, object.get("fields"));
        Serde.deserializeDocuments(this.methods, object.get("methods"));
        Serde.deserializeProperties(this.generics, object.get("variables"));
        Serde.deserializeProperties(this.interfaces, object.get("interfaces"));
    }

    public static DocumentClass fromJava(ClassInfo info) {
        DocumentClass document = new DocumentClass();
        document.name = info.getName();
        document.isAbstract = info.isAbstract();
        document.isInterface = info.isInterface();
        document.parent = info.getSuperClass() != null ? Serde.deserializeFromJavaType(info.getSuperClassType()) : null;
        document.interfaces.addAll(info.getInterfaceTypes().stream().map(Serde::deserializeFromJavaType).toList());
        info.getFieldInfo().stream().map(DocumentField::fromJava).forEach(document.fields::add);
        info.getMethodInfo().stream().map(DocumentMethod::fromJava).forEach(document.methods::add);
        return document;
    }

    @Override
    public DocumentClass merge(DocumentClass other) {
        DocumentClass document = copy();

        return document;
    }

    @Override
    public DocumentClass copy() {
        DocumentClass document = new DocumentClass();
        document.name = name;
        document.parent = parent;
        document.properties.addAll(properties);
        document.methods.addAll(methods);
        document.fields.addAll(fields);
        return document;
    }
}
