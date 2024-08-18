package moe.wolfgirl.probejs.lang.typescript.code.type;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;

import java.util.Collection;
import java.util.List;

public class TSStaticType extends TSClassType {
    public TSStaticType(ClassPath classPath) {
        super(classPath);
    }

    @Override
    public Collection<ImportInfo> getUsedImports() {
        return List.of(ImportInfo.importStatic(classPath));
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        var name = declaration.getSymbol(classPath);
        return List.of(ImportInfo.STATIC_TEMPLATE.formatted(name));
    }
}
