package moe.wolfgirl.probejs.lang.typescript.code.ts;

import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

import java.util.List;

public class ReexportDeclaration extends VariableDeclaration {

    public ReexportDeclaration(String symbol, BaseType type) {
        super(symbol, type);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        return List.of("export import %s = %s".formatted(symbol, type.line(declaration, BaseType.FormatType.RETURN)));
    }
}
