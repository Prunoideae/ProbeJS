package moe.wolfgirl.probejs.jdoc.property;

import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.jdoc.Serde;

public class PropertyAssign extends AbstractProperty<PropertyAssign> {
    private boolean shieldOriginal = false;
    private PropertyType<?> type;

    @Override
    public PropertyAssign copy() {
        PropertyAssign assign = new PropertyAssign();
        assign.shieldOriginal = shieldOriginal;
        assign.type = type;
        return assign;
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("shield", shieldOriginal);
        object.add("assign", type.serialize());
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        if (object.has("shield"))
            shieldOriginal = object.get("shield").getAsBoolean();
        type = (PropertyType<?>) Serde.deserializeProperty(object.get("assign").getAsJsonObject());
    }

    public boolean isShieldOriginal() {
        return shieldOriginal;
    }

    public PropertyType<?> getType() {
        return type;
    }

    public PropertyAssign type(PropertyType<?> type) {
        this.type = type;
        return this;
    }
}
