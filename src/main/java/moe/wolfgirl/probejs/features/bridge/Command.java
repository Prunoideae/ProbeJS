package moe.wolfgirl.probejs.features.bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class Command {


    public abstract String identifier();

    public abstract JsonElement handle(JsonObject payload);

    public record Payload(String id, String command, JsonObject payload) {

    }
}