package moe.wolfgirl.probejs.typescript.document.types.special;

import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;

import java.util.Collection;
import java.util.List;
import java.util.Set;

// Similar to ClassPath but refer to a type inside the namespace, so use ClassPath when imported,
// but actually generates "Namespace.Type" when formatting.
public class NamespacedType extends Type {
    public ClassPath classPath;
    public String typeName;

    public  NamespacedType(ClassPath classPath, String typeName) {
        this.classPath = classPath;
        this.typeName = typeName;
    }

    @Override
    public Set<ClassPath> getImports() {
        return Set.of(classPath);
    }

    @Override
    public List<String> format(int indent) {
        return List.of(resolveSymbol(classPath) + "." + typeName);
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return List.of();
    }
}
