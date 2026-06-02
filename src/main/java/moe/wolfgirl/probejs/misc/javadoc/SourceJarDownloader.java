package moe.wolfgirl.probejs.misc.javadoc;

import com.google.gson.reflect.TypeToken;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.network.chat.Component;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceJarDownloader {
    private final Map<String, String> urlToSourceJars = new HashMap<>();
    private final Path baseDir;

    public SourceJarDownloader(Path baseDir) {
        this.baseDir = baseDir;
    }

    public void load(Path mappingJson) {
        if (Files.notExists(mappingJson)) return;
        try {
            String content = Files.readString(mappingJson);
            var type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> loaded = ProbeJS.GSON.fromJson(content, type);
            if (loaded != null) {
                urlToSourceJars.putAll(loaded);
            }
        } catch (IOException e) {
            GameUtils.logException(e);
        }
    }

    public void save(Path mappingJson) {
        try {
            Files.createDirectories(mappingJson.getParent());
            String json = ProbeJS.GSON_WRITER.toJson(urlToSourceJars);
            Files.writeString(mappingJson, json);
        } catch (IOException e) {
            GameUtils.logException(e);
        }
    }

    /**
     * Download the source jars from the given URLs, return the paths to the downloaded jars.
     * This should only be called inside ProbeJSPlugin::initialize, otherwise the progress will not be displayed.
     *
     * @param urls      the URLs to download the source jars from
     * @param overwrite whether to overwrite the existing source jar if it has already been downloaded, default to false
     * @return the paths to the downloaded source jars
     */
    public List<Path> downloadSourceJars(List<String> urls, boolean overwrite) {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            List<Path> downloadedPaths = new ArrayList<>();

            // Save original DumpState progress to restore after downloading
            int originalProgress = GameStates.DUMP_STATE.progress;
            int originalMaxProgress = GameStates.DUMP_STATE.maxProgress;
            GameStates.DUMP_STATE.setMaxProgress(urls.size());
            GameStates.DUMP_STATE.setProgress(0);

            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                GameStates.DUMP_STATE.setStatus(
                        Component.literal("Downloading (" + (i + 1) + "/" + urls.size() + ")...")
                );

                Path downloaded = downloadSourceJar(client, url, overwrite);
                if (downloaded != null) {
                    downloadedPaths.add(downloaded);
                }

                GameStates.DUMP_STATE.incrementProgress(1);
            }

            // Restore original DumpState progress, counting the download phase as one step
            GameStates.DUMP_STATE.setMaxProgress(originalMaxProgress);
            GameStates.DUMP_STATE.setProgress(originalProgress + 1);

            return downloadedPaths;
        } catch (IOException e) {
            GameUtils.logException(e);
            return List.of();
        }
    }


    /**
     * Download the source jar from the given URL, return the path to the downloaded jar.
     * Resolves the filename from the Content-Disposition header if possible, otherwise uses the last segment of the URL path.
     * Will update the progress status in DUMP_STATE, but need to restore the max progress and current progress after downloading.
     *
     * @param url the URL to download the source jar from
     * @return the path to the downloaded source jar
     */
    private Path downloadSourceJar(CloseableHttpClient client, String url, boolean overwrite) {
        if (urlToSourceJars.containsKey(url) && !overwrite) {
            var path = Path.of(urlToSourceJars.get(url));
            if (Files.exists(path)) return path;
        }

        try {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                }

                // Resolve filename from Content-Disposition header, fallback to URL path
                String filename = null;
                Header contentDisposition = response.getFirstHeader("Content-Disposition");
                if (contentDisposition != null) {
                    filename = fileNameFromHeader(contentDisposition.getValue());
                }
                if (filename == null || filename.isEmpty()) {
                    String urlPath = request.getURI().getPath();
                    filename = urlPath.substring(urlPath.lastIndexOf('/') + 1);
                    if (filename.isEmpty()) filename = "downloaded-source.jar";
                }

                Path targetPath = baseDir.resolve(filename);
                Files.createDirectories(baseDir);
                Files.copy(entity.getContent(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                String absolutePath = targetPath.toAbsolutePath().toString();
                urlToSourceJars.put(url, absolutePath);
                return targetPath;
            }
        } catch (Exception e) {
            GameUtils.logException(e);
            return null;
        }
    }

    /**
     * Extracts the filename value from a Content-Disposition header.
     * Handles both quoted and unquoted filename values.
     *
     * @param headerValue the raw Content-Disposition header value
     * @return the extracted filename, or null if not found
     */
    private static String fileNameFromHeader(String headerValue) {
        for (String part : headerValue.split(";")) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                String filename = part.substring("filename=".length());
                if (filename.startsWith("\"") && filename.endsWith("\"")) {
                    filename = filename.substring(1, filename.length() - 1);
                }
                return filename;
            }
        }
        return null;
    }
}
