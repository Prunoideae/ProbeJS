package moe.wolfgirl.next.typescript.code.member;

import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.type.BaseType;
import moe.wolfgirl.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public record ParamDecl(String name, BaseType type, boolean varArg, boolean optional) {
    public String format(int index, Declaration declaration) {
        String result = Util.isNameSafe(name) ? name : "arg%d".formatted(index);
        if (varArg) result = "...%s".formatted(result);
        return "%s%s: %s".formatted(
                result,
                optional ? "?" : "",
                type.line(declaration, BaseType.FormatType.INPUT)
        );
    }

    public static String formatParams(List<ParamDecl> params, Declaration declaration) {
        List<String> formattedParams = new ArrayList<>();
        ListIterator<ParamDecl> it = params.listIterator();
        while (it.hasNext()) {
            int index = it.nextIndex();
            ParamDecl param = it.next();
            formattedParams.add(param.format(index, declaration));
        }
        return "(%s)".formatted(String.join(", ", formattedParams));
    }
}
