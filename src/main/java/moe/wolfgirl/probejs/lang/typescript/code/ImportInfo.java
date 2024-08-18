package moe.wolfgirl.probejs.lang.typescript.code;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;

import java.util.function.UnaryOperator;

public record ImportInfo(ClassPath classPath, Type type) {
    public static final UnaryOperator<String> ORIGINAL = s -> s;
    public static final String INPUT_TEMPLATE = "%s$$Type";
    public static final String STATIC_TEMPLATE = "%s$$Static";

    public enum Type {
        ORIGINAL(ImportInfo.ORIGINAL),
        TYPE(INPUT_TEMPLATE::formatted),
        STATIC(STATIC_TEMPLATE::formatted);

        private final UnaryOperator<String> formatter;

        Type(UnaryOperator<String> formatter) {
            this.formatter = formatter;
        }

        public String applyTemplate(String name) {
            return formatter.apply(name);
        }
    }

    public static ImportInfo original(ClassPath path) {
        return new ImportInfo(path, Type.ORIGINAL);
    }

    public static ImportInfo type(ClassPath path) {
        return new ImportInfo(path, Type.TYPE);
    }

    public static ImportInfo importStatic(ClassPath path) {
        return new ImportInfo(path, Type.STATIC);
    }

    public ImportInfo asType(Type type) {
        return new ImportInfo(this.classPath, type);
    }
}
