package moe.wolfgirl.probejs.features.bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.features.autolint.LintCommand;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Interop with VSCode via websocket
 */
public class ProbeServer extends WebSocketServer {
    public static final Command[] REGISTRY = new Command[]{
            new PingCommand(),
            new LintCommand(),
    };

    private final Map<String, Command> dispatcher = new HashMap<>();

    public ProbeServer(int port) {
        super(new InetSocketAddress(port));

        for (Command command : REGISTRY) {
            dispatcher.put(command.identifier(), command);
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Command.Payload payload = ProbeJS.GSON.fromJson(message, Command.Payload.class);
        Command handler = dispatcher.get(payload.command());
        if (handler == null) return;
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return;
        currentServer.execute(() -> {
            JsonObject response = new JsonObject();
            response.addProperty("id", payload.id());
            try {
                response.add("payload", handler.handle(payload.payload()));
            } catch (Throwable throwable) {
                response.addProperty("error", throwable.getMessage());
            }
            ProbeJS.LOGGER.info(response);
            conn.send(ProbeJS.GSON.toJson(response));
        });
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }

    public void broadcast(String event, JsonElement payload) {
        JsonObject pack = new JsonObject();
        pack.addProperty("event", event);
        pack.add("payload", payload);
        this.broadcast(ProbeJS.GSON.toJson(pack));
    }
}
