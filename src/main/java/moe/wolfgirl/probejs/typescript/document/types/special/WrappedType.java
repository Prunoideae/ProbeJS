package moe.wolfgirl.probejs.typescript.document.types.special;

import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class WrappedType extends Type {
    public String formatter;
    public Type wrapped;

    public WrappedType(String formatter, Type wrapped) {
        this.formatter = formatter;
        this.wrapped = wrapped;
    }


    @Override
    public Set<ClassPath> getImports() {
        return wrapped.getImports();
    }

    @Override
    public List<String> format(int indent) {
        return List.of(formatter.formatted(wrapped.first()));
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return List.of(wrapped);
    }
}
