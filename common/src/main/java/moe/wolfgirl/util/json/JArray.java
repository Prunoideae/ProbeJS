package moe.wolfgirl.util.json;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class JArray implements IJsonBuilder<JsonArray> {

    public static JArray create() {
        return new JArray();
    }

    private JArray() {
    }

    private final List<IJsonBuilder<?>> members = new ArrayList<>();

    public JArray ifThen(boolean condition, Consumer<JArray> action) {
        if (condition) {
            action.accept(this);
        }
        return this;
    }

    public JArray add(IJsonBuilder<?> member) {
        members.add(member);
        return this;
    }

    public JArray addAll(Iterable<IJsonBuilder<?>> members) {
        for (IJsonBuilder<?> member : members) {
            if (member != null)
                this.members.add(member);
        }
        return this;
    }

    public JArray addAll(Stream<IJsonBuilder<?>> members) {
        members.filter(Objects::nonNull)
                .forEach(this.members::add);
        return this;
    }

    @Override
    public JsonArray serialize() {
        JsonArray array = new JsonArray();
        for (IJsonBuilder<?> member : members) {
            array.add(member.serialize());
        }
        return array;
    }

    @Override
    public String toString() {
        return serialize().toString();
    }
}
