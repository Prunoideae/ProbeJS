package moe.wolfgirl.probejs.next.typescript;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.PackageTree;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistry;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;

import java.util.HashMap;
import java.util.Map;

public class SpecialDocuments implements DocumentRegistry, DocumentRegistrar {
    public static final SpecialDocuments INSTANCE = new SpecialDocuments();

    private final Map<ClassPath, Code> documents = new HashMap<>();
    private final Map<ClassPath, Code> globals = new HashMap<>();

    @Override
    public Code getDocument(ClassPath classPath) {
        return documents.get(classPath);
    }

    @Override
    public Code getInputAlias(ClassPath classPath) {
        return null;
    }

    @Override
    public Code getGlobal(ClassPath classPath) {
        return globals.get(classPath);
    }

    @Override
    public void clear() {
        documents.clear();
        globals.clear();
    }

    public void addDocument(ClassPath classPath, Code code) {
        if (!classPath.getBaseName().equals("@special")) {
            throw new IllegalArgumentException("SpecialDocuments only accepts class paths created by ClassPath.special");
        }
        documents.put(classPath, code);
    }


    public void addGlobal(ClassPath classPath, Code code) {
        if (!classPath.getBaseName().equals("@special")) {
            throw new IllegalArgumentException("SpecialDocuments only accepts class paths created by ClassPath.special");
        }
        globals.put(classPath, code);
    }

    public PackageTree resolveTree() {
        PackageTree tree = new PackageTree();
        for (var entry : documents.entrySet()) {
            tree.addClassPath(entry.getKey());
        }
        for (var entry : globals.entrySet()) {
            tree.addClassPath(entry.getKey());
        }
        return tree;
    }


    @Override
    public void addInputAlias(ClassPath classPath, Type type) {
        throw new UnsupportedOperationException("Input aliases are not supported in SpecialDocuments. Define directly or use Documents instead.");
    }
}
