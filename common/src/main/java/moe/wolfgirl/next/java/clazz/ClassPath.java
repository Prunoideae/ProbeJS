package moe.wolfgirl.next.java.clazz;

import dev.latvian.mods.kubejs.util.UtilsJS;
import moe.wolfgirl.next.utils.RemapperUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record ClassPath(List<String> parts) {
    public ClassPath(String className) {
        this(Arrays.stream(className.split("\\.")).toList());
    }

    public ClassPath(Class<?> clazz) {
        this(RemapperUtils.getRemappedClassName(clazz));
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

    public String getTypeScriptPath() {
        return getConcatenated("/");
    }

    public Class<?> forName() throws ClassNotFoundException {
        return Class.forName(getClassPath());
    }

    public Clazz toClazz() throws ClassNotFoundException {
        return new Clazz(forName());
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
