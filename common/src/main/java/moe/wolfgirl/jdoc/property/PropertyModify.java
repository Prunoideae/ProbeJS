package moe.wolfgirl.jdoc.property;

import com.google.gson.JsonObject;
import moe.wolfgirl.jdoc.Serde;

public class PropertyModify extends AbstractProperty<PropertyModify> {
    private int ordinal;
    private String name;
    private PropertyType<?> newType;

    public PropertyModify() {
    }

    public PropertyModify(int ordinal, PropertyType<?> newType, String name) {
        this.ordinal = ordinal;
        this.newType = newType;
        this.name = name;
    }


    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("index", ordinal);
        if (newType != null)
            object.add("newType", newType.serialize());
        if (name != null)
            object.addProperty("name", name);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        ordinal = object.get("index").getAsInt();
        if (object.has("newType"))
            newType = (PropertyType<?>) Serde.deserializeProperty(object.get("newType").getAsJsonObject());
        if (object.has("name"))
            name = object.get("name").getAsString();
    }

    @Override
    public PropertyModify copy() {
        return new PropertyModify(ordinal, newType, name);
    }

    public int getOrdinal() {
        return ordinal;
    }

    public PropertyType<?> getNewType() {
        return newType;
    }

    public String getName() {
        return name;
    }
}
