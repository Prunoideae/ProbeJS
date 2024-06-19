package moe.wolfgirl.probejs.lang.typescript.code;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class Code {
    public abstract Collection<ClassPath> getUsedClassPaths();

    public abstract List<String> format(Declaration declaration);

    public String line(Declaration declaration) {
        return format(declaration).getFirst();
    }

    public Collection<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        for (ClassPath usedClassPath : getUsedClassPaths()) {
            try {
                classes.add(usedClassPath.forName());
            } catch (Throwable ignored) {
            }
        }
        return classes;
    }
}
