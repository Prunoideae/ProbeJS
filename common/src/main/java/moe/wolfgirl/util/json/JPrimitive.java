package moe.wolfgirl.util.json;

import com.google.gson.JsonPrimitive;

public class JPrimitive implements IJsonBuilder<JsonPrimitive> {

    public static JPrimitive create(Character value) {
        return new JPrimitive(value);
    }

    public static JPrimitive create(String value) {
        return new JPrimitive(value);
    }

    public static JPrimitive create(Number value) {
        return new JPrimitive(value);
    }

    public static JPrimitive create(Boolean value) {
        return new JPrimitive(value);
    }

    private final JsonPrimitive value;

    public JPrimitive(Character value) {
        this.value = new JsonPrimitive(value);
    }

    public JPrimitive(String value) {
        this.value = new JsonPrimitive(value);
    }

    public JPrimitive(Number value) {
        this.value = new JsonPrimitive(value);
    }

    public JPrimitive(boolean value) {
        this.value = new JsonPrimitive(value);
    }


    @Override
    public JsonPrimitive serialize() {
        return value;
    }

    @Override
    public String toString() {
        return serialize().toString();
    }
}
