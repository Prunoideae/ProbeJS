package moe.wolfgirl.probejs.next.typescript.base;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.PackageTree;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;

public interface DocumentRegistry {

    Code getDocument(ClassPath classPath);

    Code getInputAlias(ClassPath classPath);

    Code getGlobal(ClassPath classPath);

    PackageTree resolveTree();

    void clear();
}
