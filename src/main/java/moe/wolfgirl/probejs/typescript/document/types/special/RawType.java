package moe.wolfgirl.probejs.typescript.document.types.special;

import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RawType extends Type {
    public String raw;

    public RawType(String raw) {
        this.raw = raw;
    }

    @Override
    public Set<ClassPath> getImports() {
        return Set.of();
    }

    @Override
    public List<String> format(int indent) {
        return List.of(raw);
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return List.of();
    }
}
