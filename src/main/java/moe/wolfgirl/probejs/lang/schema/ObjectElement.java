package moe.wolfgirl.probejs.lang.schema;

import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ObjectElement extends SchemaElement<ObjectElement> {
    private final Map<String, SchemaElement<?>> members = new HashMap<>();
    private final List<String> requires = new ArrayList<>();

    public ObjectElement primitive(String name, String type, Function<PrimitiveElement, SchemaElement<?>> function) {
        members.put(name, function.apply(new PrimitiveElement(type)));
        return this;
    }

    public ObjectElement stringType(String name) {
        return stringType(name, o -> o);
    }

    public ObjectElement stringType(String name, Function<PrimitiveElement, SchemaElement<?>> function) {
        return primitive(name, "string", function);
    }

    public ObjectElement numberType(String name) {
        return numberType(name, o -> o);
    }

    public ObjectElement numberType(String name, Function<PrimitiveElement, SchemaElement<?>> function) {
        return primitive(name, "number", function);
    }

    public ObjectElement booleanType(String name) {
        return booleanType(name, o -> o);
    }

    public ObjectElement booleanType(String name, Function<PrimitiveElement, SchemaElement<?>> function) {
        return primitive(name, "boolean", function);
    }

    public ObjectElement anyType(String name) {
        members.put(name, AnyElement.INSTANCE);
        return this;
    }

    public ObjectElement object(String name) {
        return object(name, o -> o);
    }

    public ObjectElement object(String name, Function<ObjectElement, SchemaElement<?>> function) {
        members.put(name, function.apply(new ObjectElement()));
        return this;
    }

    public ObjectElement requires(String... requires) {
        this.requires.addAll(List.of(requires));
        return this;
    }

    @Override
    public String getType() {
        return "object";
    }

    @Override
    protected JsonObject toSchema() {
        JsonObject object = new JsonObject();

        var properties = new JsonObject();
        for (Map.Entry<String, SchemaElement<?>> entry : members.entrySet()) {
            String key = entry.getKey();
            SchemaElement<?> value = entry.getValue();
            properties.add(key, value.getSchema());
        }
        object.add("properties", properties);
        object.add("requires", JsonUtils.parseObject(requires));

        return object;
    }

    public static ObjectElement of() {
        return new ObjectElement();
    }
}
