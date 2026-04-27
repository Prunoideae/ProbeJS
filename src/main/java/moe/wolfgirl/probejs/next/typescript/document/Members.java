package moe.wolfgirl.probejs.next.typescript.document;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.builders.ClassBuilder;

public interface Members {

    static ClassBuilder clazz(ClassPath classPath) {
        return new ClassBuilder(classPath.getClassName());
    }
}
