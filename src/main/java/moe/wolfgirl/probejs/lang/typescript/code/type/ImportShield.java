package moe.wolfgirl.probejs.lang.typescript.code.type;

import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;

import java.util.Collection;
import java.util.List;

public class ImportShield extends BaseType {
    private final BaseType inner;
    private final ImportInfo.Type importType;

    public ImportShield(BaseType inner, ImportInfo.Type importType) {
        this.inner = inner;
        this.importType = importType;
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return inner.format(declaration, input);
    }

    @Override
    public Collection<ImportInfo> getUsedImports() {
        return inner.getUsedImportsAs(importType);
    }
}
