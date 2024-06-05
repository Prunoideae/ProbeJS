package moe.wolfgirl.next.typescript.code.type;

import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;

import java.util.Collection;
import java.util.List;

public class TSPrimitiveType extends BaseType {
    public static final TSPrimitiveType ANY = new TSPrimitiveType("any");
    public static final TSPrimitiveType BOOLEAN = new TSPrimitiveType("boolean");
    public static final TSPrimitiveType NUMBER = new TSPrimitiveType("number");
    public static final TSPrimitiveType STRING = new TSPrimitiveType("string");
    public static final TSPrimitiveType NEVER = new TSPrimitiveType("never");
    public static final TSPrimitiveType VOID = new TSPrimitiveType("void");

    /**
     * Returns a literal type of the input if it's something OK in TS,
     * otherwise, any will be returned.
     *
     * @param content a string, number or boolean
     */
    public static TSPrimitiveType literal(Object content) {
        if (!(content instanceof String || content instanceof Number || content instanceof Boolean))
            return ANY;
        return new TSPrimitiveType(ProbeJS.GSON.toJson(content));
    }

    public final String content;

    public TSPrimitiveType(String content) {
        this.content = content;
    }


    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return List.of();
    }

    @Override
    public List<String> format(Declaration declaration, boolean input) {
        return null;
    }
}
