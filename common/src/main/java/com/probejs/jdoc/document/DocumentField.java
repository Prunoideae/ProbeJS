package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.info.FieldInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.jdoc.property.PropertyValue;

import java.util.Objects;

public class DocumentField extends AbstractDocument<DocumentField> {
    private String name;
    private boolean isStatic;
    private boolean isFinal;
    private PropertyType<?> type;
    private PropertyValue<?, ?> value;

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("name", name);
        object.addProperty("static", isStatic);
        object.addProperty("final", isFinal);
        object.add("fieldType", type.serialize());
        if (value != null)
            object.add("value", value.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        name = object.get("name").getAsString();
        isStatic = object.get("static").getAsBoolean();
        isFinal = object.get("final").getAsBoolean();
        type = (PropertyType<?>) Serde.deserializeProperty(object.get("fieldType").getAsJsonObject());
        if (object.has("value"))
            value = (PropertyValue<?, ?>) Serde.deserializeProperty(object.get("value").getAsJsonObject());
    }

    public static DocumentField fromJava(FieldInfo info) {
        DocumentField document = new DocumentField();
        document.name = info.getName();
        document.isFinal = info.isFinal();
        document.isStatic = info.isStatic();
        document.type = Serde.deserializeFromJavaType(info.getType());
        document.value = Serde.getValueProperty(info.getStaticValue());
        return document;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentField that = (DocumentField) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    public String getName() {
        return name;
    }

    public PropertyType<?> getType() {
        return type;
    }

    public PropertyValue<?, ?> getValue() {
        return value;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isStatic() {
        return isStatic;
    }
}
