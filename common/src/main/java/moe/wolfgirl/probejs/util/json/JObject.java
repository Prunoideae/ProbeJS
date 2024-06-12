package moe.wolfgirl.probejs.util.json;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * NOTE: it mutates the base object.
 */
public class JObject implements IJsonBuilder<JsonObject> {
    private final JsonObject base;

    private JObject(JsonObject base) {
        this.base = base;
    }

    /**
     * Creates a JObject from a JsonObject. Note that the JObject will mutate the base object.
     *
     * @param base the base object
     * @return the JObject
     */
    public static JObject create(JsonObject base) {
        return new JObject(base);
    }

    /**
     * Creates a JObject from a new JsonObject.
     *
     * @return the JObject
     */
    public static JObject create() {
        return create(new JsonObject());
    }

    private final Map<String, IJsonBuilder<?>> members = new HashMap<>();

    public JObject ifThen(boolean condition, Consumer<JObject> action) {
        if (condition) {
            action.accept(this);
        }
        return this;
    }

    public JObject add(String key, IJsonBuilder<?> value) {
        if (value != null)
            members.put(key, value);
        return this;
    }

    public JObject addAll(Map<String, IJsonBuilder<?>> members) {
        members.forEach(this::add);
        return this;
    }

    public JObject addAll(Iterable<Pair<String, IJsonBuilder<?>>> members) {
        for (Pair<String, IJsonBuilder<?>> member : members) {
            if (member.getSecond() != null) this.members.put(member.getFirst(), member.getSecond());
        }
        return this;
    }

    public JObject addAll(Stream<Pair<String, IJsonBuilder<?>>> members) {
        members.forEach(entry -> this.members.put(entry.getFirst(), entry.getSecond()));
        return this;
    }

    public JObject addAllEntry(Stream<Map.Entry<String, IJsonBuilder<?>>> members) {
        members.forEach(entry -> this.members.put(entry.getKey(), entry.getValue()));
        return this;
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = base;
        for (Map.Entry<String, IJsonBuilder<?>> entry : members.entrySet()) {
            String key = entry.getKey();
            IJsonBuilder<?> value = entry.getValue();
            object.add(key, value.serialize());
        }
        return object;
    }

    @Override
    public String toString() {
        return serialize().toString();
    }
}
