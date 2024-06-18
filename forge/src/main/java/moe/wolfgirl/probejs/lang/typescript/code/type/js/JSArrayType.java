package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JSArrayType extends BaseType {
    public final List<BaseType> components;

    public JSArrayType(List<BaseType> components) {
        this.components = components;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (BaseType component : components) {
            paths.addAll(component.getUsedClassPaths());
        }
        return paths;
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of(
                "[%s]".formatted(components.stream()
                        .map(type -> "(%s)".formatted(type.line(declaration, input)))
                        .collect(Collectors.joining(", ")))
        );
    }
}
