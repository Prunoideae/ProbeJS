package moe.wolfgirl.probejs.legacy.lang.typescript.code.type;

import moe.wolfgirl.probejs.legacy.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.legacy.lang.typescript.Declaration;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.ImportInfo;

import java.util.Collection;
import java.util.List;

public class TSClassType extends BaseType {
    public ClassPath classPath;

    public TSClassType(ClassPath classPath) {
        this.classPath = classPath;
    }

    @Override
    public Collection<ImportInfo> getUsedImports() {
        return List.of(ImportInfo.original(classPath));
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        var name = declaration.getSymbol(classPath);
        return List.of(input == FormatType.INPUT ? ImportInfo.INPUT_TEMPLATE.formatted(name) : name);
    }
}
