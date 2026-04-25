package moe.wolfgirl.probejs.next.typescript.document.base;

import moe.wolfgirl.probejs.next.ClassPath;

import java.util.List;

public abstract class InputAlias extends Code {
    protected boolean input = false;

    public abstract List<ClassPath> getOriginalImports();

    public final List<ClassPath> getImports() {
        if (input) {
            return getOriginalImports()
                    .stream()
                    .map(classPath -> classPath.withSuffix("_"))
                    .toList();
        } else {
            return getOriginalImports();
        }
    }

    protected String resolveSymbol(ClassPath classPath) {
        if (input) {
            var modified = classPath.withSuffix("_");
            return resolvedSymbols.getOrDefault(modified, modified.getClassName());
        } else {
            return resolvedSymbols.getOrDefault(classPath, classPath.getClassName());
        }
    }
}
