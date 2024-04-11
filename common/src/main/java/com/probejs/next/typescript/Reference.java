package com.probejs.next.typescript;

import com.probejs.next.java.clazz.ClassPath;

/**
 * Tracker for name usage.
 */
public class Reference {
    private boolean outputUsed = false;
    private boolean inputUsed = false;
    public final String name;
    public final ClassPath path;

    public Reference(String name, ClassPath path) {
        this.name = name;
        this.path = path;
    }

    public String getOutput() {
        this.outputUsed = true;
        return this.name;
    }

    public String getInput() {
        this.inputUsed = true;
        // Input type is an "underscored" type.
        // An underscored type is defined as
        // type T_ = T | AdaptableType
        return this.name + "_";
    }

    public boolean shouldConvertName() {
        return !name.equals(path.getName());
    }

    private String getImportedName(boolean isInput) {
        if (shouldConvertName()) {
            return (isInput ? "%s_ as %s_" : "%s as %s").formatted(path.getName(), name);
        } else {
            return (isInput ? "%s_" : "%s").formatted(name);
        }
    }


    /**
     * Get the import statement of the currently tracked usage status.
     * <br>
     * This should be called after code content is generated.
     */
    public String getImport() {
        StringBuilder sb = new StringBuilder();
        if (outputUsed || !inputUsed) sb.append(getImportedName(false));
        if (inputUsed) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(getImportedName(true));
        }

        return "import { %s } from \"packages/%s\"".formatted(sb, path.getTypeScriptPath());
    }
}
