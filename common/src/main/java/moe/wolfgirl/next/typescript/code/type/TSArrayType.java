package moe.wolfgirl.next.typescript.code.type;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;

import java.util.Collection;
import java.util.List;

public class TSArrayType extends BaseType {
    public BaseType component;

    public TSArrayType(BaseType component) {
        this.component = component;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return component.getUsedClassPaths();
    }

    @Override
    public List<String> format(Declaration declaration) {
        return List.of("(%s)[]".formatted(component.line(declaration)));
    }
}
