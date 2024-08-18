package moe.wolfgirl.probejs.lang.typescript.code;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Code {
    public abstract Collection<ImportInfo> getUsedImports();

    public abstract List<String> format(Declaration declaration);

    public String line(Declaration declaration) {
        return format(declaration).getFirst();
    }

    public Collection<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        for (ImportInfo usedClassPath : getUsedImports()) {
            try {
                classes.add(usedClassPath.classPath().forName());
            } catch (Throwable ignored) {
            }
        }
        return classes;
    }

    public Collection<ImportInfo> getUsedImportsAs(ImportInfo.Type type) {
        return getUsedImports().stream().map(i -> i.asType(type)).collect(Collectors.toSet());
    }
}
