package moe.wolfgirl.probejs.lang.typescript;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;

import java.util.*;

public class Declaration {
    private static final String SYMBOL_TEMPLATE = "%s$%d";

    public final Map<ClassPath, Reference> references;
    private final Map<ClassPath, String> symbols;

    private final Set<String> excludedName;

    public Declaration() {
        this.references = new HashMap<>();
        this.symbols = new HashMap<>();
        this.excludedName = new HashSet<>();
    }

    public void addClass(ImportInfo path) {
        references.computeIfAbsent(path.classPath(), classPath -> {
            var name = getSymbolName(classPath);
            return new Reference(classPath, name, EnumSet.noneOf(ImportInfo.Type.class));
        }).types().add(path.type());
    }

    public void exclude(String name) {
        excludedName.add(name);
    }

    private void putSymbolName(ClassPath path, String name) {
        symbols.put(path, name);
    }

    private boolean containsSymbol(String name) {
        return excludedName.contains(name) || symbols.containsValue(name);
    }


    private String getSymbolName(ClassPath path) {
        if (!symbols.containsKey(path)) {
            String name = path.getName();
            if (!containsSymbol(name)) putSymbolName(path, name);
            else {
                int counter = 0;
                while (containsSymbol(SYMBOL_TEMPLATE.formatted(name, counter))) {
                    counter++;
                }
                putSymbolName(path, SYMBOL_TEMPLATE.formatted(name, counter));
            }
        }

        return symbols.get(path);
    }

    public String getSymbol(ClassPath path) {
        if (!this.references.containsKey(path)) {
            throw new RuntimeException("Trying to get a symbol of a classpath that is not resolved yet!");
        }
        var reference = this.references.get(path);
        return reference.symbol();
    }
}
