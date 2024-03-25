package com.probejs.features.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.ProbeJS;
import com.probejs.ProbeJSEvents;
import net.minecraft.client.Minecraft;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class Server extends WebSocketServer {

    public Server(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Commands.Payload payload = ProbeJS.GSON.fromJson(message, Commands.Payload.class);
        JsonObject response = new JsonObject();
        ProbeJSEvents.CURRENT_SERVER.execute(() -> {
            try {
                JsonElement result = Commands.process(payload);
                response.addProperty("flag", "success");
                response.add("result", result);
            } catch (Throwable e) {
                response.addProperty("flag", "error");
                response.addProperty("error", e.getMessage());
            }
            response.addProperty("id", payload.id);
            conn.send(ProbeJS.GSON.toJson(response));
        });
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }
}