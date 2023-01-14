package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.info.ClassInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyComment;
import com.probejs.jdoc.property.PropertyType;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * The Document of a class.
 */
public class DocumentClass extends AbstractDocument<DocumentClass> {
    protected String name;
    protected List<PropertyType<?>> generics = new ArrayList<>();
    protected PropertyType<?> parent;
    public Set<PropertyType<?>> interfaces = new HashSet<>();
    public Set<DocumentField> fields = new HashSet<>();
    public Set<DocumentMethod> methods = new HashSet<>();
    public Set<DocumentConstructor> constructors = new HashSet<>();

    protected boolean isAbstract = false;
    protected boolean isInterface = false;

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("className", name);
        if (parent != null) object.add("parent", parent.serialize());
        Serde.serializeCollection(object, "fields", fields);
        Serde.serializeCollection(object, "methods", methods);
        Serde.serializeCollection(object, "variables", generics, true);
        Serde.serializeCollection(object, "interfaces", interfaces, true);
        Serde.serializeCollection(object, "constructors", constructors);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        name = object.get("className").getAsString();
        if (object.has("parent"))
            parent = (PropertyType<?>) Serde.deserializeProperty(object.get("parent").getAsJsonObject());
        Serde.deserializeDocuments(this.fields, object.get("fields"));
        Serde.deserializeDocuments(this.methods, object.get("methods"));
        Serde.deserializeDocuments(this.constructors, object.get("constructors"));
        Serde.deserializeDocuments(this.generics, object.get("variables"));
        Serde.deserializeDocuments(this.interfaces, object.get("interfaces"));

    }

    public static DocumentClass fromJava(ClassInfo info) {
        DocumentClass document = new DocumentClass();
        document.name = info.getName();
        document.isAbstract = info.isAbstract();
        document.isInterface = info.isInterface();
        document.parent = info.getSuperClass() != null ? Serde.deserializeFromJavaType(info.getSuperClassType()) : null;
        document.interfaces.addAll(info.getInterfaceTypes().stream().map(Serde::deserializeFromJavaType).toList());
        document.generics.addAll(info.getParameters().stream().map(Serde::deserializeFromJavaType).toList());
        info.getFieldInfo().stream().map(DocumentField::fromJava).forEach(document.fields::add);
        info.getMethodInfo().stream().map(DocumentMethod::fromJava).forEach(document.methods::add);
        info.getConstructorInfo().stream().map(DocumentConstructor::fromJava).forEach(document.constructors::add);
        if (info.getAnnotations().stream().anyMatch(annotation -> annotation instanceof Deprecated))
            document.properties.add(new PropertyComment("@deprecated"));
        return document;
    }

    @Override
    public DocumentClass applyProperties() {
        return this;
    }

    @Override
    public DocumentClass merge(DocumentClass other) {
        //Overwrites everything basing on document.
        //Generics are not overwritten tho
        if (this == other) return this;
        DocumentClass document = other.copy();
        document.parent = parent;
        document.interfaces.addAll(interfaces);
        document.methods.addAll(methods);
        document.fields.addAll(fields);
        document.properties.addAll(properties);
        document.constructors = constructors;
        document.generics = generics;
        document.isInterface = isInterface;
        document.isAbstract = isAbstract;
        document.builtinComments.addAll(builtinComments);
        return document;
    }

    @Override
    public DocumentClass copy() {
        DocumentClass document = new DocumentClass();
        document.name = name;
        document.parent = parent;
        document.isInterface = isInterface;
        document.isAbstract = isAbstract;
        document.interfaces.addAll(interfaces);
        document.properties.addAll(properties);
        document.methods.addAll(methods);
        document.fields.addAll(fields);
        document.builtinComments.addAll(builtinComments);
        return document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentClass that = (DocumentClass) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getName() {
        return name;
    }

    public Set<DocumentConstructor> getConstructors() {
        return constructors;
    }

    public Set<DocumentMethod> getMethods() {
        return methods;
    }

    public Set<DocumentField> getFields() {
        return fields;
    }

    public Set<PropertyType<?>> getInterfaces() {
        return interfaces;
    }

    public List<PropertyType<?>> getGenerics() {
        return generics;
    }

    public PropertyType<?> getParent() {
        return parent;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isInterface() {
        return isInterface;
    }
}
