package moe.wolfgirl.probejs.lang.typescript.code.type;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;

import java.util.Collection;
import java.util.List;

public class ContextShield extends BaseType {
    private final BaseType inner;
    private final FormatType formatType;

    public ContextShield(BaseType inner, FormatType formatType) {
        this.inner = inner;
        this.formatType = formatType;
    }

    @Override
    public Collection<ImportInfo> getUsedImports() {
        return inner.getUsedImports();
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return inner.format(declaration, formatType);
    }
}
