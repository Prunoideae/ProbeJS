package moe.wolfgirl.probejs.typescript.document.base;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.typescript.ClassPath;

import java.util.Set;
import java.util.stream.Collectors;

// A Type that has alternative symbol resolution when marked as input.
// E.g. List_<String> = String[]
public abstract class InputAliased extends Type {
    protected boolean input = false;

    public InputAliased asInput() {
        this.input = true;
        return this;
    }

    public InputAliased asOutput() {
        this.input = false;
        return this;
    }

    public abstract Set<ClassPath> getOriginalImports();

    public final Set<ClassPath> getImports() {
        if (input) {
            return getOriginalImports().stream()
                    .map(classPath -> classPath.withSuffix("_"))
                    .collect(Collectors.toSet());
        } else {
            return getOriginalImports();
        }
    }

    public String resolveSymbol(ClassPath classPath) {
        if (resolvedSymbols == null) {
            ProbeJS.LOGGER.warn("Resolved symbols map is ot set when resolving symbol for %s. Override setResolvedSymbols in the caller to provide the map!".formatted(classPath));
            throw new IllegalStateException("Resolved symbols map is not set");
        }
        if (input) {
            var modified = classPath.withSuffix("_");
            return resolvedSymbols.getOrDefault(modified, modified.getClassName());
        } else {
            return resolvedSymbols.getOrDefault(classPath, classPath.getClassName());
        }
    }
}
