package moe.wolfgirl.probejs.typescript.document.types;

import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;

import java.util.*;

public class ParamType extends Type {
    public Type baseType;
    public List<Type> typeArgs;

    public ParamType(Type baseType, List<Type> typeArgs) {
        this.baseType = baseType;
        this.typeArgs = typeArgs;
    }


    @Override
    public Set<ClassPath> getImports() {
        Set<ClassPath> imports = new HashSet<>(baseType.getImports());
        for (Code typeArg : typeArgs) {
            imports.addAll(typeArg.getImports());
        }
        return Set.copyOf(imports);
    }

    @Override
    public List<String> format(int indent) {
        return List.of("%s<%s>".formatted(
                baseType.first(),
                typeArgs.stream().map(Code::first).reduce("%s, %s"::formatted).orElse("")
        ));
    }

    @Override
    public Collection<Code> getContainedTypes() {
        List<Code> types = new ArrayList<>();
        types.add(baseType);
        types.addAll(typeArgs);
        return types;
    }
}
