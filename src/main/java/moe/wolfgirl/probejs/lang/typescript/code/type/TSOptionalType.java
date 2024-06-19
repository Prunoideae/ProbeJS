package moe.wolfgirl.probejs.lang.typescript.code.type;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;

import java.util.Collection;
import java.util.List;

public class TSOptionalType extends BaseType {
    public BaseType component;

    public TSOptionalType(BaseType component) {
        this.component = component;
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of("(%s)?".formatted(component.line(declaration, input)));
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return component.getUsedClassPaths();
    }
}
