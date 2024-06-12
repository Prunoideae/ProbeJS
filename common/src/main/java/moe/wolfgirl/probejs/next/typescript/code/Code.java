package moe.wolfgirl.probejs.next.typescript.code;

import moe.wolfgirl.probejs.next.java.clazz.ClassPath;
import moe.wolfgirl.probejs.next.typescript.Declaration;

import java.util.Collection;
import java.util.List;

public abstract class Code {
    public abstract Collection<ClassPath> getUsedClassPaths();

    public abstract List<String> format(Declaration declaration);

    public String line(Declaration declaration) {
        return format(declaration).get(0);
    }
}
