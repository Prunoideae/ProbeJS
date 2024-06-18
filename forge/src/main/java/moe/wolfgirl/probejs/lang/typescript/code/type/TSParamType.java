package moe.wolfgirl.probejs.lang.typescript.code.type;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TSParamType extends BaseType {
    public BaseType baseType;
    public final List<BaseType> params;

    public TSParamType(BaseType baseType, List<BaseType> params) {
        this.baseType = baseType;
        this.params = params;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> paths = new HashSet<>(baseType.getUsedClassPaths());
        for (BaseType param : params) {
            paths.addAll(param.getUsedClassPaths());
        }
        return paths;
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of(
                "%s<%s>".formatted(
                        baseType.line(declaration, input),
                        params.stream()
                                .map(type -> "(%s)".formatted(type.line(declaration, input)))
                                .collect(Collectors.joining(", "))
                )
        );
    }
}
