package moe.wolfgirl.probejs.typescript.document.types.special;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class LiteralType extends Type {
    public String literal;

    public LiteralType(String literal) {
        this.literal = literal;
    }

    @Override
    public Set<ClassPath> getImports() {
        return Set.of();
    }

    @Override
    public List<String> format(int indent) {
        return List.of(ProbeJS.GSON.toJson(literal));
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return List.of();
    }
}
