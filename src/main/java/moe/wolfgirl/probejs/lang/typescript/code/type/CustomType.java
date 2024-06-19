package moe.wolfgirl.probejs.lang.typescript.code.type;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class CustomType extends BaseType {
    private final BiFunction<Declaration, FormatType, String> formatter;
    private final ClassPath[] imports;

    public CustomType(BiFunction<Declaration, FormatType, String> formatter, ClassPath[] imports) {
        this.formatter = formatter;
        this.imports = imports;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return List.of(imports);
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return Collections.singletonList(formatter.apply(declaration, input));
    }
}
