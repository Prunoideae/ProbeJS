package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.info.FieldInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.jdoc.property.PropertyValue;
import dev.latvian.mods.rhino.util.RemapForJS;

import java.lang.annotation.Annotation;
import java.util.Objects;

public class DocumentField extends AbstractDocument<DocumentField> {
    private String name;
    private boolean isStatic;
    private boolean isFinal;
    private boolean shouldGSON = false;
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
        if (object.has("static"))
            isStatic = object.get("static").getAsBoolean();
        if (object.has("final"))
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
        document.shouldGSON = true;
        if (info.getStaticValue() != null)
            document.value = Serde.getValueProperty(info.getStaticValue());
        info.getAnnotations().stream().filter(annotation -> annotation instanceof Deprecated).findFirst().ifPresent(annotation -> {
            document.builtinComments.add("@deprecated");
            if (((Deprecated) annotation).forRemoval()) {
                document.builtinComments.add("This field is marked to be removed in future!");
            }
        });
        return document;
    }

    @Override
    public DocumentField applyProperties() {
        return this;
    }

    @Override
    public DocumentField copy() {
        DocumentField document = new DocumentField();
        document.name = name;
        document.isStatic = isStatic;
        document.isFinal = isFinal;
        document.shouldGSON = shouldGSON;
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

    public boolean isShouldGSON() {
        return shouldGSON;
    }
}
