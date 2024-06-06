package moe.wolfgirl.next.typescript.code.member;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.Code;
import moe.wolfgirl.next.typescript.code.type.BaseType;

import java.util.Collection;
import java.util.List;

/**
 * Represents a type declaration. Standalone members are always exported.
 */
public class TypeDecl extends Code {
    public BaseType type;
    public final String symbol;

    public TypeDecl(String symbol, BaseType type) {
        this.symbol = symbol;
        this.type = type;
    }


    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return type.getUsedClassPaths();
    }

    @Override
    public List<String> format(Declaration declaration) {
        return List.of(
                "export type %s = %s;".formatted(symbol, type.line(declaration, BaseType.FormatType.INPUT))
        );
    }
}
