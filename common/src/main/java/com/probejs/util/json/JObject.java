package com.probejs.util.json;

import com.google.gson.JsonObject;
import com.probejs.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class JObject implements IJsonBuilder<JsonObject> {

    public static JObject create() {
        return new JObject();
    }

    private final Map<String, IJsonBuilder<?>> members = new HashMap<>();

    public JObject add(String key, IJsonBuilder<?> value) {
        members.put(key, value);
        return this;
    }

    public JObject addAll(Map<String, IJsonBuilder<?>> members) {
        this.members.putAll(members);
        return this;
    }

    public JObject addAll(Iterable<Pair<String, IJsonBuilder<?>>> members) {
        for (Pair<String, IJsonBuilder<?>> member : members) {
            this.members.put(member.getFirst(), member.getSecond());
        }
        return this;
    }

    public JObject addAll(Stream<Pair<String, IJsonBuilder<?>>> members) {
        members.forEach(entry -> this.members.put(entry.getFirst(), entry.getSecond()));
        return this;
    }

    @Override
    public JsonObject serialize() {

        JsonObject object = new JsonObject();
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
