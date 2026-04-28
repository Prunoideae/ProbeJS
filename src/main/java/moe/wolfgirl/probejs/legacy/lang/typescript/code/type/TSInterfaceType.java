package moe.wolfgirl.probejs.legacy.lang.typescript.code.type;

import moe.wolfgirl.probejs.legacy.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.legacy.lang.typescript.Declaration;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.ImportInfo;

import java.util.Collection;
import java.util.List;

public class TSInterfaceType extends TSClassType {

    public TSInterfaceType(ClassPath classPath) {
        super(classPath);
    }

    @Override
    public Collection<ImportInfo> getUsedImports() {
        return List.of(ImportInfo.itf(classPath));
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        var name = declaration.getSymbol(classPath);
        return List.of(ImportInfo.INTERFACE_TEMPLATE.formatted(name));
    }
}
