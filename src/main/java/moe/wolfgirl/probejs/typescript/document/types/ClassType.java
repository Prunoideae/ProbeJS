package moe.wolfgirl.probejs.typescript.document.types;

import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.InputAliased;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.types.special.NamespacedType;

import java.util.Collection;
import java.util.Collections;
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

    public NamespacedType inner(String typeName) {
        return new NamespacedType(classPath, typeName);
    }

    public Type asMaybeGeneric() {
        try {
            var variables = classPath.loadClass().getTypeParameters();
            if (variables.length == 0) return this;
            else {
                // Fill with any
                Type[] anyArgs = Collections.nCopies(variables.length, Types.ANY).toArray(new Type[0]);
                return this.withParams(anyArgs);
            }
        } catch (ClassNotFoundException e) {
            return this;
        }
    }
}
