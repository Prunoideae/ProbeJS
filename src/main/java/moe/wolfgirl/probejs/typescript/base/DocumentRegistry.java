package moe.wolfgirl.probejs.typescript.base;

import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.java.PackageTree;
import moe.wolfgirl.probejs.typescript.document.base.Code;

public interface DocumentRegistry {

    Code getDocument(ClassPath classPath);

    Code getInputAlias(ClassPath classPath);

    Code getGlobal(ClassPath classPath);

    default Code getDocument(Class<?> clazz) {
        return getDocument(new ClassPath(clazz));
    }

    default Code getInputAlias(Class<?> clazz) {
        return getInputAlias(new ClassPath(clazz));
    }

    default Code getGlobal(Class<?> clazz) {
        return getGlobal(new ClassPath(clazz));
    }

    PackageTree resolveTree();

    void clear();
}
