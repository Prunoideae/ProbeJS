package moe.wolfgirl.probejs.next.typescript.document.types.special;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class JoinedType extends Type {
    public List<Code> types;

    protected abstract String operator();

    @Override
    public List<String> format(int indent) {
        return List.of(String.join(operator(), types.stream().map(Code::first).toList()));
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return List.copyOf(types);
    }

    @Override
    public Set<ClassPath> getImports() {
        Set<ClassPath> imports = new HashSet<>();
        for (Code type : types) {
            imports.addAll(type.getImports());
        }
        return Set.copyOf(imports);
    }

    public static class UnionType extends JoinedType {
        @Override
        protected String operator() {
            return " | ";
        }
    }

    public static class IntersectionType extends JoinedType {
        @Override
        protected String operator() {
            return " & ";
        }
    }
}
