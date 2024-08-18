package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

import java.util.Collection;
import java.util.List;

public class JSTypeOfType extends BaseType {

    public final BaseType inner;

    public JSTypeOfType(BaseType inner) {
        this.inner = inner;
    }

    @Override
    public Collection<ImportInfo> getUsedImports() {
        return inner.getUsedImports();
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of(
                "typeof %s".formatted(inner.line(declaration, FormatType.RETURN))
        );
    }
}
