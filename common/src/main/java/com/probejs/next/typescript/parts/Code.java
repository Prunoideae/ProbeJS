package com.probejs.next.typescript.parts;

import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.typescript.Reference;

import java.util.List;
import java.util.Map;

/**
 * Represents one or multiple lines of content.
 */
public abstract class Code {
    private Map<ClassPath, Reference> symbols;

    public abstract List<String> getContent();

    public void setSymbols(Map<ClassPath, Reference> symbols) {
        this.symbols = symbols;
    }

    // In case the path is not translated, we don't touch the name as it's meaningless
    protected final String input(ClassPath path) {
        return symbols.containsKey(path) ? symbols.get(path).getInput() : path.getName();
    }

    protected final String output(ClassPath path) {
        return symbols.containsKey(path) ? symbols.get(path).getOutput() : path.getName();
    }
}
