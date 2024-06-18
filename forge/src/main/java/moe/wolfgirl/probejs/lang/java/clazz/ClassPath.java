package moe.wolfgirl.probejs.lang.java.clazz;

import dev.latvian.mods.kubejs.util.UtilsJS;
import moe.wolfgirl.probejs.lang.java.ClassRegistry;
import moe.wolfgirl.probejs.utils.RemapperUtils;

import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record ClassPath(List<String> parts) {
    private static List<String> transformJavaClass(Class<?> clazz) {
        String name = RemapperUtils.getRemappedClassName(clazz);
        String[] parts = name.split("\\.");
        String className = "$" + parts[parts.length - 1];
        parts[parts.length - 1] = className;
        return Arrays.stream(parts).toList();
    }

    public ClassPath(String className) {
        this(Arrays.stream(className.split("\\.")).toList());
    }

    public ClassPath(Class<?> clazz) {
        this(transformJavaClass(clazz));
    }

    public String getName() {
        return parts.get(parts.size() - 1);
    }

    public String getConcatenated(String sep) {
        return String.join(sep, parts);
    }

    public String getClassPath() {
        return getConcatenated(".");
    }

    public String getClassPathJava() {
        List<String> copy = new ArrayList<>(parts);
        String last = copy.get(copy.size() - 1);
        if (last.startsWith("$")) last = last.substring(1);
        copy.set(copy.size() - 1, last);
        return String.join(".", copy);
    }

    public String getTypeScriptPath() {
        return getConcatenated("/");
    }

    public Class<?> forName() throws ClassNotFoundException {
        return Class.forName(getClassPathJava());
    }

    public List<String> getGenerics() throws ClassNotFoundException {
        TypeVariable<?>[] variables = forName().getTypeParameters();
        return Arrays.stream(variables).map(TypeVariable::getName).toList();
    }

    public Clazz toClazz() {
        return ClassRegistry.REGISTRY.foundClasses.get(this);
    }

    public List<String> getPackage() {
        List<String> classPath = new ArrayList<>(parts);
        classPath.remove(classPath.size() - 1);
        return classPath;
    }

    public String getConcatenatedPackage(String sep) {
        return String.join(sep, getPackage());
    }

    public Path getDirPath(Path base) {
        return base.resolve(getConcatenatedPackage("/"));
    }

    public Path makePath(Path base) {
        Path full = getDirPath(base);
        if (Files.notExists(full)) {
            UtilsJS.tryIO(() -> Files.createDirectories(full));
        }
        return full;
    }
}
