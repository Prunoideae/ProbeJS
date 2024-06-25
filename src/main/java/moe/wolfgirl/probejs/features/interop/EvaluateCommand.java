package moe.wolfgirl.probejs.features.interop;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.NativeJavaObject;
import moe.wolfgirl.probejs.features.bridge.Command;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.utils.JsonUtils;

public class EvaluateCommand extends Command {
    @Override
    public String identifier() {
        return "evaluate";
    }

    @Override
    public JsonElement handle(JsonObject payload) {
        var scriptType = payload.get("scriptType").getAsString();
        var content = payload.get("content").getAsString();

        ScriptManager scriptManager = switch (scriptType) {
            case "startup_scripts" -> KubeJS.getStartupScriptManager();
            case "client_scripts" -> KubeJS.getClientScriptManager();
            case "server_scripts" -> GameUtils.getServerScriptManager();
            case null, default -> null;
        };

        if (scriptManager == null) throw new RuntimeException("Unable to get script manager.");
        KubeJSContext context = (KubeJSContext) scriptManager.contextFactory.enter();
        Object result = context.evaluateString(context.topLevelScope, content, "probejsEvaluator", 1, null);
        if (result instanceof NativeJavaObject nativeJavaObject) {
            result = nativeJavaObject.unwrap();
        }
        JsonElement jsonElement = JsonUtils.parseObject(result);
        if (jsonElement == JsonNull.INSTANCE && result != null) jsonElement = new JsonPrimitive(result.toString());
        return jsonElement;
    }
}
