package moe.wolfgirl.probejs.jdoc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.ProbePaths;
import moe.wolfgirl.probejs.jdoc.java.ClassInfo;
import moe.wolfgirl.probejs.jdoc.document.DocumentClass;
import moe.wolfgirl.probejs.jdoc.property.AbstractProperty;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.util.UtilsJS;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Manager {
    private static boolean docsDownloaded = false;
    private static final String TIMESTAMP_API = "https://static.wolfgirl.moe/api/timestamp?path=probejs/docs-1.19.2-6.1.zip";
    private static final String DOWNLOAD_API = "https://static.wolfgirl.moe/object-service/checked/probejs/docs-1.19.2-6.1.zip?timestamp=%s";

    public static void downloadDocs() throws IOException {
        if (docsDownloaded) return;
        ProbeJS.LOGGER.info("Checking docs timestamps...");
        long remoteTimestamp;
        try {
            URL timestampApi = new URL(TIMESTAMP_API);
            BufferedReader in = new BufferedReader(new InputStreamReader(timestampApi.openStream()));
            remoteTimestamp = Long.parseLong(in.readLine());
        } catch (Exception e) {
            ProbeJS.LOGGER.warn("Cannot connect to remote server, docs are not checked or downloaded!");
            ProbeJS.LOGGER.warn("The server might come back online later, this is not an error.");
            return;
        }
        Path docsPath = ProbePaths.CACHE.resolve("docs");
        if (ProbeConfig.INSTANCE.docsTimestamp != remoteTimestamp || !Files.exists(docsPath)) {
            ProbeJS.LOGGER.info("Found timestamp mismatch (local=%s, remote=%s), downloading docs from remote.".formatted(ProbeConfig.INSTANCE.docsTimestamp, remoteTimestamp));
            if (Files.exists(docsPath)) {
                FileUtils.deleteDirectory(docsPath.toFile());
            }
            UtilsJS.tryIO(() -> Files.createDirectories(docsPath));
            URL downloadUrl = new URL(DOWNLOAD_API.formatted(ProbeConfig.INSTANCE.docsTimestamp));
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(downloadUrl.openStream()));
            ZipEntry entry = zipInputStream.getNextEntry();
            byte[] buffer = new byte[1024];
            while (entry != null) {
                try (FileOutputStream outputStream = new FileOutputStream(docsPath.resolve(entry.getName()).toFile())) {
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                }
                ProbeJS.LOGGER.info("Downloaded doc: %s".formatted(entry.getName()));
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
            ProbeConfig.INSTANCE.docsTimestamp = remoteTimestamp;
            ProbeConfig.INSTANCE.save();
        } else {
            ProbeJS.LOGGER.info("Timestamps are matched (local = remote = %s), no need to update docs.".formatted(remoteTimestamp));
        }
        docsDownloaded = true;
    }

    public static List<DocumentClass> loadFetchedClassDoc() throws IOException {
        List<DocumentClass> docs = new ArrayList<>();
        Path docsPath = ProbePaths.CACHE.resolve("docs");
        if (Files.exists(docsPath)) {
            for (File file : Objects.requireNonNull(docsPath.toFile().listFiles())) {
                docs.addAll(loadJsonClassDoc(file.toPath()));
                ProbeJS.LOGGER.info("Loaded fetched doc: %s".formatted(file.getName()));
            }
        }
        return docs;
    }

    public static List<DocumentClass> loadJavaClasses(Set<Class<?>> classes) {
        List<DocumentClass> javaClasses = new ArrayList<>();
        for (Class<?> clazz : classes) {
            DocumentClass document = DocumentClass.fromJava(ClassInfo.getOrCache(clazz));
            javaClasses.add(document);
        }
        return javaClasses;
    }

    public static List<DocumentClass> loadJsonClassDoc(Path path) throws IOException {
        JsonObject docsObject = ProbeJS.GSON.fromJson(Files.newBufferedReader(path), JsonObject.class);
        if (docsObject.has("properties")) {
            List<AbstractProperty<?>> properties = new ArrayList<>();
            Serde.deserializeDocuments(properties, docsObject.get("properties"));
            for (AbstractProperty<?> property : properties) {
                if (property instanceof IConditional condition && !condition.test()) {
                    return List.of();
                }
            }
        }
        JsonArray docsArray = docsObject.get("classes").getAsJsonArray();
        List<DocumentClass> documents = new ArrayList<>();
        for (JsonElement element : docsArray) {
            if (Serde.deserializeDocument(element.getAsJsonObject()) instanceof DocumentClass documentClass)
                documents.add(documentClass);
        }
        return documents;
    }

    public static List<DocumentClass> loadModDocuments() throws IOException {
        List<DocumentClass> documents = new ArrayList<>();
        for (Mod mod : Platform.getMods()) {
            Optional<Path> list = mod.findResource("probejs.documents.txt");
            if (list.isPresent()) {
                for (String entry : Files.lines(list.get()).toList()) {
                    if (!entry.endsWith(".json")) {
                        ProbeJS.LOGGER.warn("Skipping non-JsonDoc entry - %s".formatted(entry));
                        continue;
                    }
                    Optional<Path> entryPath = mod.findResource(entry);
                    if (entryPath.isPresent()) {
                        ProbeJS.LOGGER.info("Loading document inside jar - %s".formatted(entry));
                        List<DocumentClass> jsonDoc = loadJsonClassDoc(entryPath.get());
                        documents.addAll(jsonDoc);
                    } else {
                        ProbeJS.LOGGER.warn("Document from file is not found - %s".formatted(entry));
                    }
                }
            }
        }
        return documents;
    }

    public static List<DocumentClass> loadUserDocuments() throws IOException {
        List<DocumentClass> documents = new ArrayList<>();
        for (File file : Objects.requireNonNull(ProbePaths.DOCS.toFile().listFiles())) {
            if (!file.getName().endsWith(".json"))
                continue;
            Path path = Paths.get(file.toURI());
            documents.addAll(loadJsonClassDoc(path));
        }
        return documents;
    }

    @SafeVarargs
    public static Map<String, DocumentClass> mergeDocuments(List<DocumentClass>... sources) {
        Map<String, DocumentClass> documents = new HashMap<>();
        for (List<DocumentClass> source : sources) {
            for (DocumentClass clazz : source) {
                if (!documents.containsKey(clazz.getName())) {
                    documents.put(clazz.getName(), clazz);
                } else {
                    documents.put(clazz.getName(), documents.get(clazz.getName()).merge(clazz));
                }
            }
        }
        return documents;
    }

}
