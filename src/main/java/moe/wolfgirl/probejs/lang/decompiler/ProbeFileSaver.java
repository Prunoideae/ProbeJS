package moe.wolfgirl.probejs.lang.decompiler;

import moe.wolfgirl.probejs.lang.decompiler.parser.ParsedDocument;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class ProbeFileSaver implements IResultSaver {
    public final Map<ClassPath, ParsedDocument> result = new HashMap<>();
    private Runnable callback;
    public int classCount = 0;

    @Override
    public void saveFolder(String path) {

    }

    @Override
    public void copyFile(String source, String path, String entryName) {

    }

    @Override
    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
        String[] parts = qualifiedName.split("/");
        parts[parts.length - 1] = "$" + parts[parts.length - 1];
        ClassPath classPath = new ClassPath(List.of(parts));
        result.put(classPath, new ParsedDocument(content));
        classCount++;
        if (callback != null) callback.run();
    }

    @Override
    public void createArchive(String path, String archiveName, Manifest manifest) {

    }

    @Override
    public void saveDirEntry(String path, String archiveName, String entryName) {

    }

    @Override
    public void copyEntry(String source, String path, String archiveName, String entry) {

    }

    @Override
    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
        ClassPath classPath = new ClassPath(qualifiedName.replace("/", "."));
        result.put(classPath, new ParsedDocument(content));
        classCount++;
        if (callback != null) callback.run();
    }

    @Override
    public void closeArchive(String path, String archiveName) {

    }

    public void writeTo(Path base) throws IOException {
        for (Map.Entry<ClassPath, ParsedDocument> entry : result.entrySet()) {
            ClassPath classPath = entry.getKey();
            ParsedDocument s = entry.getValue();
            s.getParamTransformations();

            Path full = classPath.makePath(base);
            try (var out = Files.newBufferedWriter(full.resolve(classPath.getName() + ".java"))) {
                String[] lines = s.getCode().split("\\n");
                out.write(Arrays.stream(lines)
                        .filter(l -> !l.strip().startsWith("// $VF: renamed"))
                        .collect(Collectors.joining("\n"))
                );
            }
        }
    }

    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        for (Map.Entry<ClassPath, ParsedDocument> entry : result.entrySet()) {
            ClassPath classPath = entry.getKey();
            ParsedDocument parsedDocument = entry.getValue();

            if (parsedDocument.isMixinClass()) continue;
            try {
                classes.add(classPath.forName());
            } catch (Throwable ignored) {
            }
        }
        return classes;
    }

    public ProbeFileSaver callback(Runnable callback) {
        this.callback = callback;
        return this;
    }
}
