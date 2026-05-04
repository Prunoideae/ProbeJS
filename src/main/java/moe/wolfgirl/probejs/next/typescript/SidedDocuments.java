package moe.wolfgirl.probejs.next.typescript;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.PackageTree;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistry;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;

import java.util.HashMap;
import java.util.Map;

public class SidedDocuments implements DocumentRegistry, DocumentRegistrar {
    public static final SidedDocuments INSTANCE = new SidedDocuments();

    private final Map<ClassPath, Code> documents = new HashMap<>();
    private final Map<ClassPath, Code> globals = new HashMap<>();

    @Override
    public void addGlobal(ClassPath classPath, Code code) {
        if (notSided(classPath)) {
            throw new IllegalArgumentException("SidedDocuments only accepts class paths created by ClassPath.sided");
        }
        globals.put(classPath, code);
    }

    @Override
    public void addDocument(ClassPath classPath, Code code) {
        if (notSided(classPath)) {
            throw new IllegalArgumentException("SidedDocuments only accepts class paths created by ClassPath.sided");
        }
        documents.put(classPath, code);
    }

    @Override
    public void addInputAlias(ClassPath classPath, Type type) {
        throw new UnsupportedOperationException("Input aliases are not supported in SidedDocuments. Define directly or use Documents instead.");
    }

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
    public void clear() {
        documents.clear();
        globals.clear();
    }

    private boolean notSided(ClassPath classPath) {
        var sideOnlyBase = classPath.getBaseName().equals("@side-only");
        var firstSegment = classPath.segments().getFirst();
        var sidedBase = firstSegment.equals("client") || firstSegment.equals("server") || firstSegment.equals("startup");
        return !sideOnlyBase || !sidedBase;
    }
}
