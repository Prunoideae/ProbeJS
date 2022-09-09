package com.probejs.jdoc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.info.ClassInfo;
import com.probejs.jdoc.document.DocumentClass;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Manager {
    public static List<DocumentClass> loadJavaClasses(Set<Class<?>> classes) {
        List<DocumentClass> javaClasses = new ArrayList<>();
        for (Class<?> clazz : classes) {
            DocumentClass document = DocumentClass.fromJava(ClassInfo.getOrCache(clazz));
            javaClasses.add(document);
        }
        return javaClasses;
    }

    public static List<DocumentClass> loadJSONDocument(Path path) throws IOException {
        JsonArray docsJson = ProbeJS.GSON.fromJson(Files.newBufferedReader(path), JsonArray.class);
        List<DocumentClass> documents = new ArrayList<>();
        for (JsonElement element : docsJson) {
            DocumentClass document = (DocumentClass) Serde.deserializeDocument(element.getAsJsonObject());
            if (document != null)
                documents.add(document);
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
                        ProbeJS.LOGGER.info("Loading document inside jar - %s".formatted(entryPath));
                        List<DocumentClass> jsonDoc = loadJSONDocument(entryPath.get());
                        documents.addAll(jsonDoc);
                    } else {
                        ProbeJS.LOGGER.warn("Document from file is not found - %s".formatted(entryPath));
                    }
                }
            }
        }
        return documents;
    }

    public static List<DocumentClass> loadUserDocuments() throws IOException {
        List<DocumentClass> documents = new ArrayList<>();
        for (File file : Objects.requireNonNull(ProbePaths.DOCS.toFile().listFiles())) {
            Path path = Paths.get(file.toURI());
            documents.addAll(loadJSONDocument(path));
        }
        return documents;
    }

    public static Map<String, DocumentClass> mergeDocuments(List<DocumentClass>... sources) {
        Map<String, DocumentClass> documents = new HashMap<>();
        for (List<DocumentClass> source : sources) {
            
        }
        return documents;
    }

}
