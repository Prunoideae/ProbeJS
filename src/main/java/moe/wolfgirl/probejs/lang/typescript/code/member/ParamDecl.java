package moe.wolfgirl.probejs.lang.typescript.code.member;

import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.utils.NameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public final class ParamDecl {
    public String name;
    public BaseType type;
    public boolean varArg;
    public boolean optional;

    public ParamDecl(String name, BaseType type, boolean varArg, boolean optional) {
        this.name = name;
        this.type = type;
        this.varArg = varArg;
        this.optional = optional;
    }

    public String format(int index, Declaration declaration) {
        String result = NameUtils.isNameSafe(name) ? name : "arg%d".formatted(index);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParamDecl) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.type, that.type) &&
                this.varArg == that.varArg &&
                this.optional == that.optional;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, varArg, optional);
    }

    @Override
    public String toString() {
        return "ParamDecl[" +
                "name=" + name + ", " +
                "type=" + type + ", " +
                "varArg=" + varArg + ", " +
                "optional=" + optional + ']';
    }

}
