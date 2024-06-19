package moe.wolfgirl.probejs.lang.typescript.code.ts;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.member.CommentableCode;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

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
