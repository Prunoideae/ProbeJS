package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.info.FieldInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyType;

public class DocumentField extends AbstractDocument<DocumentField> {
    private String name;
    private boolean isStatic;
    private boolean isFinal;
    private PropertyType type;

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("name", name);
        object.addProperty("static", isStatic);
        object.addProperty("final", isFinal);
        object.add("fieldType", type.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        name = object.get("name").getAsString();
        isStatic = object.get("static").getAsBoolean();
        isFinal = object.get("final").getAsBoolean();
        type = (PropertyType) Serde.deserializeProperty(object.get("fieldType").getAsJsonObject());
    }

    public boolean matchField(FieldInfo info) {
        if (!name.equals(info.getName())) {
            return false;
        }
        if (info.isFinal() != isFinal || info.isStatic() != isStatic)
            return false;
        return type.equalsToJavaType(info.getType());
    }

    public static DocumentField fromJava(FieldInfo info) {
        DocumentField document = new DocumentField();
        document.name = info.getName();
        document.isFinal = info.isFinal();
        document.isStatic = info.isStatic();
        document.type = Serde.deserializeFromJavaType(info.getType());
        return document;
    }

    @Override
    public DocumentField merge(DocumentField other) {
        return other.copy();
    }

    @Override
    public DocumentField copy() {
        DocumentField document = new DocumentField();
        document.name = name;
        document.isStatic = isStatic;
        document.isFinal = isFinal;
        document.type = type;
        document.properties.addAll(properties);
        return document;
    }
}
