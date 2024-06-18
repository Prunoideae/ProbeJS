package moe.wolfgirl.probejs.lang.typescript.code.type;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;

import java.util.Collection;
import java.util.List;

public class TSClassType extends BaseType {
    public ClassPath classPath;

    public TSClassType(ClassPath classPath) {
        this.classPath = classPath;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return List.of(classPath);
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of(declaration.getSymbol(classPath, input == FormatType.INPUT));
    }
}
