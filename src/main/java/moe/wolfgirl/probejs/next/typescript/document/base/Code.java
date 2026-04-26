package moe.wolfgirl.probejs.next.typescript.document.base;

import moe.wolfgirl.probejs.next.ClassPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Code {
    protected final Map<ClassPath, String> resolvedSymbols = new HashMap<>();

    public abstract Set<ClassPath> getImports();

    public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
        this.resolvedSymbols.clear();
        this.resolvedSymbols.putAll(resolvedSymbols);
    }

    public abstract List<String> format(int indent);

    public List<String> format() {
        return format(0);
    }

    public String first() {
        return format().getFirst();
    }

    protected String resolveSymbol(ClassPath classPath) {
        return resolvedSymbols.getOrDefault(classPath, classPath.getClassName());
    }
}
