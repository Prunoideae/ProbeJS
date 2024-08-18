package moe.wolfgirl.probejs.lang.java.clazz;

import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.probejs.lang.java.ClassRegistry;

import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record ClassPath(List<String> parts) {
    private static List<String> transformJavaClass(Class<?> clazz) {
        String name = clazz.getName();
        String[] parts = name.split("\\.");
        return Arrays.stream(parts).toList();
    }

    public ClassPath(String className) {
        this(Arrays.stream(className.split("\\.")).toList());
    }

    public ClassPath(Class<?> clazz) {
        this(transformJavaClass(clazz));
    }

    public String getName() {
        return "$" + parts.getLast();
    }

    public String getConcatenated(String sep) {
        return String.join(sep, parts);
    }

    public String getClassPath() {
        return getConcatenated(".");
    }

    public String getClassPathJava() {
        List<String> copy = new ArrayList<>(parts);
        String last = copy.getLast();
        if (last.startsWith("$")) last = last.substring(1);
        copy.set(copy.size() - 1, last);
        return String.join(".", copy);
    }

    public String getTypeScriptPath() {
        return getConcatenated(".");
    }

    @HideFromJS
    public Class<?> forName() throws ClassNotFoundException {
        return Class.forName(getClassPathJava());
    }

    public List<String> getGenerics() throws ClassNotFoundException {
        TypeVariable<?>[] variables = forName().getTypeParameters();
        return Arrays.stream(variables).map(TypeVariable::getName).toList();
    }

    @HideFromJS
    public Clazz toClazz() {
        return ClassRegistry.REGISTRY.foundClasses.get(this);
    }

    public List<String> getPackage() {
        List<String> classPath = new ArrayList<>(parts);
        classPath.removeLast();
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
