package moe.wolfgirl.probejs.features.interop;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.features.bridge.Command;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class ReloadCommand extends Command {
    @Override
    public String identifier() {
        return "reload";
    }

    @Override
    public JsonElement handle(JsonObject payload) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) throw new RuntimeException("No current server found.");

        switch (payload.get("scriptType").getAsString()) {
            case "server_scripts" -> server.kjs$runCommand("kubejs reload server-scripts");
            case "startup_scripts" -> server.kjs$runCommand("kubejs reload startup-scripts");
            case "client_scripts" -> server.kjs$runCommand("kubejs reload client-scripts");
            case "reload" -> server.kjs$runCommand("reload");
        }

        return JsonNull.INSTANCE;
    }
}
