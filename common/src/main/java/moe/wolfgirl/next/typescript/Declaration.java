package moe.wolfgirl.next.typescript;

import moe.wolfgirl.next.java.clazz.ClassPath;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Directly represents the content of a .d.ts file.
 * <br>
 */
public class Declaration {
    private final ClassPath path;
    private final Map<ClassPath, Reference> symbols;

    public Declaration(ClassPath path, Set<ClassPath> symbols) {
        this.path = path;
        // Load symbols and resolve the names
        // if the name conflicts, rename them into $0, $1, etc.
        this.symbols = new HashMap<>();
    }

    public List<String> compileImports() {
        return symbols.values()
                .stream()
                .map(Reference::getImport)
                .collect(Collectors.toList());
    }
}
