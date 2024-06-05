package moe.wolfgirl.next.typescript.code.type;

import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.Code;

import java.util.List;

public abstract class BaseType extends Code {
    public final List<String> format(Declaration declaration) {
        return format(declaration, false);
    }

    public abstract List<String> format(Declaration declaration, boolean input);

    public String line(Declaration declaration, boolean input) {
        return format(declaration, input).get(0);
    }
}
