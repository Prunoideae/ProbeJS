package moe.wolfgirl.probejs.typescript.document.types.special;

import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OptionalType extends Type {
    public Type componentType;

    public OptionalType(Type componentType) {
        this.componentType = componentType;
    }

    @Override
    public Set<ClassPath> getImports() {
        return componentType.getImports();
    }

    @Override
    public List<String> format(int indent) {
        return List.of("(%s) | undefined".formatted(componentType.first()));
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return List.of(componentType);
    }
}
