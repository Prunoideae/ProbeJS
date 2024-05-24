package moe.wolfgirl.jdoc.property;

import com.google.gson.JsonObject;
import moe.wolfgirl.jdoc.Serde;

public class PropertyReturns extends AbstractProperty<PropertyReturns> {
    private PropertyType<?> type;

    public PropertyReturns() {
    }

    public PropertyReturns(PropertyType<?> type) {
        this.type = type;
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.add("returns", type.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        type = (PropertyType<?>) Serde.deserializeProperty(object.get("returns").getAsJsonObject());
    }

    @Override
    public PropertyReturns copy() {
        return new PropertyReturns(type);
    }

    public PropertyType<?> getType() {
        return type;
    }
}
