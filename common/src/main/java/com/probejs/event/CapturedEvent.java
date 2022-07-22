package com.probejs.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.script.ScriptType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class CapturedEvent {
    @Nonnull
    private final Class<? extends EventJS> captured;
    @Nullable
    private final String sub;
    @Nonnull
    private final String id;
    @Nonnull
    private final EnumSet<ScriptType> scriptTypes;

    private final boolean cancellable;

    public CapturedEvent(@Nonnull Class<? extends EventJS> captured, @Nonnull String id, @Nullable String sub, @Nonnull EnumSet<ScriptType> type, boolean cancellable) {
        this.captured = captured;
        this.sub = sub;
        this.id = id;
        this.scriptTypes = type;
        this.cancellable = cancellable;
    }

    public boolean hasSub() {
        return sub != null;
    }

    @Nullable
    public String getSub() {
        return sub;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public EnumSet<ScriptType> getScriptTypes() {
        return scriptTypes;
    }

    public String getFormattedTypeString() {
        return scriptTypes.stream().map(scriptType -> "**%s**".formatted(scriptType.name)).collect(Collectors.joining(", "));
    }

    public boolean isCancellable() {
        return cancellable;
    }

    @Nonnull
    public Class<? extends EventJS> getCaptured() {
        return captured;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("sub", sub);
        json.addProperty("class", getCaptured().getName());
        JsonArray types = new JsonArray();
        scriptTypes.forEach(script -> types.add(script.name()));
        json.add("type", types);
        json.addProperty("cancellable", cancellable);
        return json;
    }

    @SuppressWarnings("unchecked")
    public static Optional<CapturedEvent> fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        Class<?> clazz;
        try {
            clazz = Class.forName(json.get("class").getAsString());
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
        EnumSet<ScriptType> types = EnumSet.noneOf(ScriptType.class);
        if (json.has("type")) {
            JsonArray jArray = json.get("type").getAsJsonArray();
            jArray.forEach(jElement -> types.add(ScriptType.valueOf(jElement.getAsString())));
        }
        String sub = json.has("sub") ? json.get("sub").getAsString() : null;
        boolean cancellable = json.has("cancellable") && json.get("cancellable").getAsBoolean();
        return Optional.of(new CapturedEvent((Class<? extends EventJS>) clazz, id, sub, types, cancellable));
    }

}
