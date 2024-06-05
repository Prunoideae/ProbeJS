package moe.wolfgirl.next.typescript.code.member;

import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.type.BaseType;
import moe.wolfgirl.util.Util;

public record ParamDecl(String name, BaseType type, boolean varArg) {
    public String format(int index, Declaration declaration) {
        String result = Util.isNameSafe(name) ? name : "arg%d".formatted(index);
        if (varArg) result = "...%s".formatted(result);
        return "%s: %s".formatted(result, type.line(declaration));
    }
}
