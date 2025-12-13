package moe.wolfgirl.probejs.lang.java.clazz;

import dev.latvian.mods.rhino.util.HideFromJS;

import java.io.IOException;
import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record ClassPath(List<String> parts) {
    public static final ClassPath EMPTY = new ClassPath(List.of());

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
            try {
                Files.createDirectories(full);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return full;
    }

    public String getFileKey() {
        if (parts.size() == 1) return getClassPath();
        return String.join(".", parts.subList(0, Math.min(4, parts.size() - 1)));
    }

    public String toDiff(ClassPath base) {
        var common = countCommonPrefix(this.parts, base.parts);
        var diff = new ArrayList<>(this.parts);
        Collections.fill(diff.subList(0, common), "");
        return String.join(".", diff);
    }

    public ClassPath fromDiff(String diff) {
        var parts = diff.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                parts[i] = this.parts.get(i);
            } else {
                break;
            }
        }
        return new ClassPath(List.of(parts));
    }

    private static <T> int countCommonPrefix(List<T> a, List<T> b) {
        var sizeCompare = Integer.min(a.size(), b.size());

        var common = 0;
        for (int i = 0; i < sizeCompare; i++) {
            if (Objects.equals(a.get(i), b.get(i))) {
                common++;
            } else {
                break;
            }
        }
        return common;
    }
}
