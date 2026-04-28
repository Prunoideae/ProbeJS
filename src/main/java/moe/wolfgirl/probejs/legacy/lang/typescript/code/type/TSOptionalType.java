package moe.wolfgirl.probejs.legacy.lang.typescript.code.type;

import moe.wolfgirl.probejs.legacy.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.legacy.lang.typescript.Declaration;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.ImportInfo;

import java.util.*;

public class TSOptionalType extends BaseType {
    public BaseType component;
    private static final BaseType OPTIONAL_BASE = Types.type(Optional.class);
    private static final ImportInfo OPTIONAL_IMPORT = ImportInfo.original(new ClassPath(Optional.class));
    public TSOptionalType(BaseType component) {
        this.component = component;
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return input == FormatType.RETURN ?
                Types.parameterized(OPTIONAL_BASE, component).format(declaration, input) :
                List.of("(%s)?".formatted(component.line(declaration, input)));
    }

    @Override
    public Collection<ImportInfo> getUsedImports() {
        Set<ImportInfo> importInfos = new HashSet<>(component.getUsedImports());
        importInfos.add(OPTIONAL_IMPORT);
        return importInfos;
    }
}
