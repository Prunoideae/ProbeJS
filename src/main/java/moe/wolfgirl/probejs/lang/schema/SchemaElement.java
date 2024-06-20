package moe.wolfgirl.probejs.lang.schema;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.utils.JsonUtils;

import java.util.*;

public abstract class SchemaElement<T extends SchemaElement<T>> {
    protected final List<Object> enums = new ArrayList<>();
    protected final Map<String, Object> additional = new HashMap<>();

    public abstract String getType();

    protected abstract JsonObject toSchema();

    public JsonObject getSchema() {
        JsonObject object = toSchema();
        object.addProperty("type", getType());
        if (!enums.isEmpty()) object.add("enum", JsonUtils.parseObject(enums));
        for (Map.Entry<String, Object> entry : additional.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            JsonElement element = JsonUtils.parseObject(value);
            if (element == JsonNull.INSTANCE) continue;
            object.add(key, element);
        }
        return object;
    }

    @SuppressWarnings("unchecked")
    protected final T self() {
        return (T) this;
    }

    public T enums(Object... values) {
        enums.addAll(Arrays.asList(values));
        return self();
    }

    public T additionalField(String key, Object value) {
        additional.put(key, value);
        return self();
    }

    public ArrayElement asArray() {
        return new ArrayElement(this);
    }
}
