package moe.wolfgirl.probejs.lang.typescript.code.member;

import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

import java.util.Collection;
import java.util.List;

/**
 * Represents a type declaration. Standalone members are always exported.
 */
public class TypeDecl extends CommentableCode {
    public BaseType type;
    public final String symbol;

    public TypeDecl(String symbol, BaseType type) {
        this.symbol = symbol;
        this.type = type;
    }


    @Override
    public Collection<ImportInfo> getUsedImports() {
        return type.getUsedImportsAs(ImportInfo.Type.TYPE);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        return List.of(
                "export type %s = %s;".formatted(symbol, type.line(declaration, BaseType.FormatType.INPUT))
        );
    }
}
