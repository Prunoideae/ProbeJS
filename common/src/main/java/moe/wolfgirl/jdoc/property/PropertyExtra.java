package moe.wolfgirl.jdoc.property;

import com.google.gson.JsonObject;
import moe.wolfgirl.jdoc.Serde;

@Deprecated(forRemoval = true)
public class PropertyExtra extends AbstractProperty<PropertyExtra> {

    private PropertyType<?> type;

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.add("extra", type.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        type = (PropertyType<?>) Serde.deserializeProperty(object.get("extra").getAsJsonObject());
    }

    @Override
    public PropertyExtra copy() {
        PropertyExtra extra = new PropertyExtra();
        extra.type = type;
        return extra;
    }

    public PropertyType<?> getType() {
        return type;
    }
}
