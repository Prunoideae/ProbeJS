package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

import java.util.function.Function;

public record JSParam(String name, boolean optional, BaseType type) {
    public String format(Declaration declaration, BaseType.FormatType formatType, Function<String, String> nameGetter) {
        return "%s%s: %s".formatted(nameGetter.apply(name), optional ? "?" : "", type.line(declaration, formatType));
    }
}
