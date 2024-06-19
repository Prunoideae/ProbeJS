package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

public record JSParam(String name, boolean optional, BaseType type) {
    public String format(Declaration declaration, BaseType.FormatType formatType) {
        return "%s%s: %s".formatted(name, optional ? "?" : "", type.line(declaration, formatType));
    }
}
