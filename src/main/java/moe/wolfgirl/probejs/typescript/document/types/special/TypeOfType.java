package moe.wolfgirl.probejs.typescript.document.types.special;

import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TypeOfType extends Type {
    public Type type;

    public TypeOfType(Type type) {
        this.type = type;
    }

    @Override
    public Set<ClassPath> getImports() {
        return type.getImports();
    }

    @Override
    public List<String> format(int indent) {
        return List.of("typeof %s".formatted(type.first()));
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return List.of(type);
    }
}
