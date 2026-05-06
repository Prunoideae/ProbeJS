package moe.wolfgirl.probejs.typescript.document.types;

import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ArrayType extends Type {
    public Type componentType;

    public ArrayType(Type componentType) {
        this.componentType = componentType;
    }

    @Override
    public Set<ClassPath> getImports() {
        return componentType.getImports();
    }

    @Override
    public List<String> format(int indent) {
        return List.of("%s[]".formatted(componentType.first()));
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return List.of(componentType);
    }
}
