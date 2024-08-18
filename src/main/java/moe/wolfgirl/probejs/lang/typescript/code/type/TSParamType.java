package moe.wolfgirl.probejs.lang.typescript.code.type;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;

import java.util.*;
import java.util.stream.Collectors;

public class TSParamType extends BaseType {
    public BaseType baseType;
    public List<BaseType> params;

    public TSParamType(BaseType baseType, List<BaseType> params) {
        this.baseType = baseType;
        this.params = new ArrayList<>(params);
    }

    @Override
    public Collection<ImportInfo> getUsedImports() {
        Set<ImportInfo> paths = new HashSet<>(baseType.getUsedImports());
        for (BaseType param : params) {
            paths.addAll(param.getUsedImports());
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
