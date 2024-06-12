package moe.wolfgirl.probejs.next.typescript.code.ts;

import moe.wolfgirl.probejs.next.java.clazz.ClassPath;
import moe.wolfgirl.probejs.next.typescript.Declaration;
import moe.wolfgirl.probejs.next.typescript.code.member.CommentableCode;
import moe.wolfgirl.probejs.next.typescript.code.type.BaseType;

import java.util.Collection;
import java.util.List;

public class VariableDeclaration extends CommentableCode {

    public String symbol;
    public BaseType type;

    public VariableDeclaration(String symbol, BaseType type) {
        this.symbol = symbol;
        this.type = type;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return type.getUsedClassPaths();
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        return List.of("const %s: %s".formatted(symbol, type.line(declaration)));
    }
}
