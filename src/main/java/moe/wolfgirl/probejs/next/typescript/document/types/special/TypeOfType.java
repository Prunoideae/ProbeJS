package moe.wolfgirl.probejs.next.typescript.document.types.special;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
        return List.of();
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return List.of(type);
    }
}
