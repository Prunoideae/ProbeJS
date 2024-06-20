package moe.wolfgirl.probejs.lang.schema;

import com.google.gson.JsonObject;

public class PrimitiveElement extends SchemaElement<PrimitiveElement> {

    private final String type;

    public PrimitiveElement(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    protected JsonObject toSchema() {
        return new JsonObject();
    }
}
