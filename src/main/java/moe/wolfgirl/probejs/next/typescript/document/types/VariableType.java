package moe.wolfgirl.probejs.next.typescript.document.types;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.InputAliased;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class VariableType extends InputAliased {
    public final String name;
    public Type typeInfo;

    public VariableType(String name, @Nullable Type typeInfo) {
        this.name = name;
        this.typeInfo = typeInfo;
    }


    @Override
    public List<String> format(int indent) {
        return List.of(name);
    }

    public String formatWithBound() {
        if (typeInfo == null) {
            return name;
        } else {
            return "%s extends %s".formatted(name, typeInfo.first());
        }
    }

    @Override
    public Set<ClassPath> getOriginalImports() {
        return typeInfo == null ? Set.of() : typeInfo.getImports();
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return typeInfo == null ? List.of() : List.of(typeInfo);
    }
}
