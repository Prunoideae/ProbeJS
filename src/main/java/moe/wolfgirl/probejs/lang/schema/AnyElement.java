package moe.wolfgirl.probejs.lang.schema;

import com.google.gson.JsonObject;

public class AnyElement extends SchemaElement<AnyElement> {

    public static final AnyElement INSTANCE = new AnyElement();

    private AnyElement() {

    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    protected JsonObject toSchema() {
        return null;
    }

    @Override
    public JsonObject getSchema() {
        return new JsonObject();
    }
}
