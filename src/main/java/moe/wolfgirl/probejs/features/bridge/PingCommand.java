package moe.wolfgirl.probejs.features.bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class PingCommand extends Command {
    @Override
    public String identifier() {
        return "ping";
    }

    @Override
    public JsonElement handle(JsonObject payload) {
        return new JsonPrimitive("pong");
    }
}
