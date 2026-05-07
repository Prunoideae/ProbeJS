package moe.wolfgirl.probejs.typescript.document.types.special;

import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class JoinedType extends Type {
    public List<Type> types;

    protected abstract String operator();

    public JoinedType(List<Type> types) {
        this.types = types;
    }

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
        public UnionType(List<Type> types) {
            super(types);
        }

        @Override
        protected String operator() {
            return " | ";
        }
    }

    public static class IntersectionType extends JoinedType {
        public IntersectionType(List<Type> types) {
            super(types);
        }

        @Override
        protected String operator() {
            return " & ";
        }
    }
}
