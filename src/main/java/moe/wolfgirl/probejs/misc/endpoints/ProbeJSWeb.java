package moe.wolfgirl.probejs.misc.endpoints;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.tree.CommandNode;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;
import dev.latvian.mods.kubejs.web.KJSHTTPRequest;
import dev.latvian.mods.kubejs.web.LocalWebServerRegistry;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.misc.DelegatedSourceStack;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;

import java.io.IOException;

public class ProbeJSWeb {

    public static void register(LocalWebServerRegistry registry) {
        registry.get("/api/probejs/list-commands", ProbeJSWeb::getCommands);
    }

    public static void registerWithAuth(LocalWebServerRegistry registry) {
        registry.post("/api/probejs/run-command", ProbeJSWeb::runCommand);
    }

    private static HTTPResponse runCommand(KJSHTTPRequest req) {
        try {
            var command = ProbeJS.GSON.fromJson(req.mainBody().text(), JsonObject.class).get("command").getAsString();
            var server = GameUtils.getCurrentServer();
            if (server == null) return HTTPStatus.BAD_REQUEST.json("\"Server not started\"");
            var player = server.getPlayerList().getPlayers().getFirst();
            var css = new DelegatedSourceStack(player.createCommandSourceStack(), player.position(), player);
            server.getCommands().performPrefixedCommand(css, command);
            return HTTPResponse.ok().json(ProbeJS.GSON.toJson(css.getCommandMessages()));
        } catch (IOException e) {
            return HTTPStatus.INTERNAL_ERROR.json("\"Unable to read request body\"");
        }
    }

    private static HTTPResponse getCommands(KJSHTTPRequest req) {
        var player = Minecraft.getInstance().player;
        if (player == null) return HTTPStatus.INTERNAL_ERROR.json("\"Not in game yet.\"");
        var result = new JsonObject();
        result.addProperty("playerName", player.getGameProfile().getName());
        result.add("commands", provideCommands());
        return HTTPResponse.ok().json(ProbeJS.GSON.toJson(result));
    }

    public static JsonArray provideCommands() {
        var server = GameUtils.getCurrentServer();
        if (server == null) throw new RuntimeException("Not in game yet, no command dispatcher can be read.");

        var dispatcher = server.getCommands().getDispatcher();
        var css = server.createCommandSourceStack();
        var commands = dispatcher.getSmartUsage(dispatcher.getRoot(), css);

        JsonArray results = new JsonArray();
        for (CommandNode<CommandSourceStack> command : commands.keySet()) {
            var parseResults = dispatcher.parse(command.getName(), css);
            if (!parseResults.getContext().getNodes().isEmpty()) {
                var commandUsages = dispatcher.getSmartUsage(Iterables.getLast(parseResults.getContext().getNodes()).getNode(), css);
                for (String value : commandUsages.values()) {
                    results.add("/" + parseResults.getReader().getString() + " " + value);
                }
            }
        }

        return results;
    }
}
