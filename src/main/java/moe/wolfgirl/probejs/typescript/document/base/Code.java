package moe.wolfgirl.probejs.typescript.document.base;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.typescript.ClassPath;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Code {
    protected Map<ClassPath, String> resolvedSymbols;

    public abstract Set<ClassPath> getImports();

    public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
        this.resolvedSymbols = resolvedSymbols;
    }

    public abstract List<String> format(int indent);

    public List<String> format() {
        return format(0);
    }

    public String first() {
        return format().getFirst();
    }

    /**
     * Streams formatted output directly to a writer without building intermediate lists.
     * Default implementation delegates to {@link #format(int)}; subclasses that accumulate
     * many lines (e.g. ClassDecl) should override to write line-by-line.
     */
    public void writeTo(BufferedWriter writer, int indent) throws IOException {
        for (String line : format(indent)) {
            writer.write(line);
            writer.write("\n");
        }
    }

    protected String resolveSymbol(ClassPath classPath) {
        if (resolvedSymbols == null) {
            ProbeJS.LOGGER.warn("Resolved symbols map is ot set when resolving symbol for %s. Override setResolvedSymbols in the caller to provide the map!".formatted(classPath));
            throw new IllegalStateException("Resolved symbols map is not set");
        }
        return resolvedSymbols.getOrDefault(classPath, classPath.getClassName());
    }
}
