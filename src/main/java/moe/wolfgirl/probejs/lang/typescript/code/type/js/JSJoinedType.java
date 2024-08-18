package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class JSJoinedType extends BaseType {
    public final String delimiter;
    public final List<BaseType> types;

    protected JSJoinedType(String delimiter, List<BaseType> types) {
        this.delimiter = " %s ".formatted(delimiter);
        this.types = types;
    }


    @Override
    public Collection<ImportInfo> getUsedImports() {
        Set<ImportInfo> paths = new HashSet<>();
        for (BaseType type : types) {
            paths.addAll(type.getUsedImports());
        }
        return paths;
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of(
                types.stream()
                        .map(type -> "(%s)".formatted(type.line(declaration, input)))
                        .collect(Collectors.joining(delimiter))
        );
    }

    public static class Union extends JSJoinedType {
        public Union(List<BaseType> types) {
            super("|", types);
        }
    }

    public static class Intersection extends JSJoinedType {

        public Intersection(List<BaseType> types) {
            super("&", types);
        }
    }
}
