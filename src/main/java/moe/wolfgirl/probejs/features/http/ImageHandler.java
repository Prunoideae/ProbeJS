package moe.wolfgirl.probejs.features.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.utils.ImageUtils;
import net.minecraft.world.item.ItemStack;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ImageHandler implements HttpHandler {
    private final Map<String, Optional<byte[]>> imageCache = new HashMap<>();

    public ImageHandler(Map<String, byte[]> images) {
        for (Map.Entry<String, byte[]> entry : images.entrySet()) {
            String key = entry.getKey();
            byte[] image = entry.getValue();
            imageCache.put(key, Optional.of(image));
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        List<NameValuePair> params = URLEncodedUtils.parse(
                exchange.getRequestURI().getRawQuery(),
                StandardCharsets.UTF_8
        );

        if (params.size() != 1) {
            this.respondWithError(exchange);
            return;
        }
        var param = params.getFirst();
        if (!param.getName().equals("stack")) {
            this.respondWithError(exchange);
            return;
        }

        if (imageCache.containsKey(param.getValue())) {
            this.respondWithImage(exchange, imageCache.get(param.getValue()));
            return;
        }

        ItemStack parsed = ItemStackJS.wrap(RegistryAccessContainer.BUILTIN, param.getValue());
        Optional<byte[]> image;
        if (parsed.isEmpty()) image = Optional.empty();
        else {
            try (var renderedImage = ImageUtils.renderItem(parsed, 32, 32)) {
                image = renderedImage == null ? Optional.empty() : Optional.of(renderedImage.asByteArray());
                ProbeJS.LOGGER.info("Rendered image of itemstack %s".formatted(param.getValue()));
            }
        }
        imageCache.put(param.getName(), image);
        this.respondWithImage(exchange, image);
    }


    private void respondWithImage(HttpExchange exchange, Optional<byte[]> image) throws IOException {
        if (image.isPresent()) {
            exchange.getResponseHeaders().add("Content-Type", "image/png");
            byte[] content = image.get();
            exchange.sendResponseHeaders(200, content.length);
            exchange.getResponseBody().write(content);
            exchange.close();
        } else {
            this.respondWithError(exchange);
        }

    }

    private void respondWithError(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
    }

    public void save(Path writeTo) {

    }
}
