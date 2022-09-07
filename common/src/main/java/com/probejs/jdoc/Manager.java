package com.probejs.jdoc;

import com.google.gson.JsonArray;
import com.probejs.ProbeJS;
import com.probejs.info.ClassInfo;
import com.probejs.jdoc.document.DocumentClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Manager {
    public static Map<String, DocumentClass> loadJavaClasses(Set<Class<?>> classes) {
        HashMap<String, DocumentClass> javaClasses = new HashMap<>();
        for (Class<?> clazz : classes) {
            DocumentClass document = DocumentClass.fromJava(ClassInfo.getOrCache(clazz));
            javaClasses.put(document.getName(), document);
        }
        return javaClasses;
    }

    public static Map<String, DocumentClass> loadJSONDocuments(Set<File> documents) throws FileNotFoundException {
        Map<String, DocumentClass> loadedDocuments = new HashMap<>();
        for (File path : documents) {
            JsonArray loadedClasses = ProbeJS.GSON.fromJson(new BufferedReader(new FileReader(path)), JsonArray.class);
            List<DocumentClass> classes = new ArrayList<>();
            Serde.deserializeDocuments(classes, loadedClasses);
            for (DocumentClass clazz : classes) {
                if (loadedDocuments.containsKey(clazz.getName())) {
                    loadedDocuments.get(clazz.getName()).merge(clazz);
                } else {
                    loadedDocuments.put(clazz.getName(), clazz);
                }
            }
        }
        return loadedDocuments;
    }
}
