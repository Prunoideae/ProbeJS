package moe.wolfgirl.next.typescript;

import moe.wolfgirl.next.java.clazz.ClassPath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Declaration {
    private static final String SYMBOL_TEMPLATE = "%s$%d";

    public final Map<ClassPath, Reference> references;
    private final Set<String> symbols;

    public Declaration() {
        this.references = new HashMap<>();
        this.symbols = new HashSet<>();
    }

    public void addClass(ClassPath path) {
        // So we determine a unique symbol that is safe to use at startup
        this.references.put(path, new Reference(path, getSymbolName(path)));
    }

    private String getSymbolName(ClassPath path) {
        String name = path.getName();
        if (!symbols.contains(name)) {
            symbols.add(name);
            return name;
        }

        int counter = 0;
        while (symbols.contains(SYMBOL_TEMPLATE.formatted(name, counter))) {
            counter++;
        }
        name = SYMBOL_TEMPLATE.formatted(name, counter);
        symbols.add(name);
        return name;
    }

    public String getSymbol(ClassPath path) {
        if (!this.references.containsKey(path)) {
            throw new RuntimeException("Trying to get a symbol of a classpath that is not resolved yet!");
        }
        return this.references.get(path).symbol();
    }
}
