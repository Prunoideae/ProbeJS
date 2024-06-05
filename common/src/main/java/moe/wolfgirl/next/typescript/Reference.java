package moe.wolfgirl.next.typescript;

import moe.wolfgirl.next.java.clazz.ClassPath;

public record Reference(ClassPath classPath, String symbol) {
    public String getImport() {
        return symbol.equals(classPath.getName()) ? "import {%s_, %s} from \"packages/%s\"".formatted(
                symbol, symbol, classPath.getTypeScriptPath()
        ) : "import {%s_ as %s_, %s as %s} from \"packages/%s\"".formatted(
                classPath.getName(), symbol, classPath.getName(), symbol, classPath.getTypeScriptPath()
        );
    }
}
