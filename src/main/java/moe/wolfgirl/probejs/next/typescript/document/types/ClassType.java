package moe.wolfgirl.probejs.next.typescript.document.types;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.InputAliased;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ClassType extends InputAliased {
    public ClassPath classPath;

    public ClassType(ClassPath classPath) {
        this.classPath = classPath;
    }

    @Override
    public Set<ClassPath> getOriginalImports() {
        return Set.of(classPath);
    }

    @Override
    public List<String> format(int indent) {
        return List.of(resolveSymbol(classPath));
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return List.of();
    }
}
