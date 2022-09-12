package com.probejs.jdoc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.info.ClassInfo;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.property.AbstractProperty;
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
                        ProbeJS.LOGGER.info("Loading document inside jar - %s".formatted(entryPath));
                        List<DocumentClass> jsonDoc = loadJsonClassDoc(entryPath.get());
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
            if (!file.getName().endsWith(".json"))
                continue;
            Path path = Paths.get(file.toURI());
            documents.addAll(loadJsonClassDoc(path));
        }
        return documents;
    }

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
