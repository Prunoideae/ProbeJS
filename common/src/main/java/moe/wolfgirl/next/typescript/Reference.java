package moe.wolfgirl.next.typescript;

import moe.wolfgirl.next.java.clazz.ClassPath;

public record Reference(ClassPath classPath, String symbol) {
    public String getImport() {
        // Underscores can be recognized by using a global export
        return symbol.equals(classPath.getName()) ? "import {%s} from \"packages/%s\"".formatted(
                symbol, classPath.getTypeScriptPath()
        ) : "import {%s as %s} from \"packages/%s\"".formatted(
                classPath.getName(), symbol, classPath.getTypeScriptPath()
        );
    }
}
