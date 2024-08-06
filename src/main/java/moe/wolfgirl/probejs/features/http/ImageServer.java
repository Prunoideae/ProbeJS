package moe.wolfgirl.probejs.features.http;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class ImageServer {
    public static HttpServer serve() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 1234), 0);
        server.setExecutor(null);
        server.createContext("/image", new ImageHandler(Map.of()));
        server.start();
        return server;
    }
}
