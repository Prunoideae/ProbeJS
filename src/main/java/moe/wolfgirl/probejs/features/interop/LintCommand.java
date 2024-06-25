package moe.wolfgirl.probejs.features.interop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.features.bridge.Command;
import moe.wolfgirl.probejs.lang.linter.Linter;
import moe.wolfgirl.probejs.lang.linter.LintingWarning;

import java.io.IOException;

public class LintCommand extends Command {
    @Override
    public String identifier() {
        return "lint";
    }

    @Override
    public JsonElement handle(JsonObject payload) {
        String scriptType = payload.get("script_type").getAsString();
        Linter linter = switch (scriptType) {
            case "client" -> Linter.CLIENT_SCRIPT.get();
            case "server" -> Linter.SERVER_SCRIPT.get();
            case "startup" -> Linter.STARTUP_SCRIPT.get();
            case null, default -> throw new RuntimeException("Unknown script type %s".formatted(scriptType));
        };

        try {
            var warnings = linter.lint();
            var result = new JsonArray();
            for (LintingWarning warning : warnings) {
                result.add(ProbeJS.GSON.toJsonTree(warning));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
