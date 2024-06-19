package moe.wolfgirl.probejs.lang.typescript.code.type;

import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.Code;

import java.util.List;

public abstract class BaseType extends Code {
    public final List<String> format(Declaration declaration) {
        return format(declaration, FormatType.RETURN);
    }

    public abstract List<String> format(Declaration declaration, FormatType input);

    public String line(Declaration declaration, FormatType input) {
        return format(declaration, input).get(0);
    }

    // Stuffs for convenience

    public TSArrayType asArray() {
        return new TSArrayType(this);
    }

    public enum FormatType {
        INPUT,
        RETURN,
        VARIABLE
    }
}
