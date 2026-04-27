package moe.wolfgirl.probejs.next;

import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.probejs.next.utils.TSPathProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ClassPath implements TSPathProvider<ClassPath> {
    private final String baseName;
    private final List<String> segments;
    private static final Map<ClassPath, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    public static ClassPath special(String className) {
        return new ClassPath("@special", List.of(className.split("\\.")));
    }

    public static ClassPath sided(ScriptType side, String className) {
        String sidePrefix = switch (side) {
            case CLIENT -> "client";
            case SERVER -> "server";
            case STARTUP -> "startup";
        };
        List<String> segments = new ArrayList<>();
        segments.add(sidePrefix);
        segments.addAll(List.of(className.split("\\.")));
        return new ClassPath("@side-only", segments);
    }

    public ClassPath(String baseName, List<String> segments) {
        this.baseName = baseName;
        this.segments = segments;
    }

    public ClassPath(List<String> segments) {
        this.baseName = "@package";
        this.segments = segments;
    }

    public ClassPath(String className) {
        this(List.of(className.split("\\.")));
    }

    public ClassPath(Class<?> clazz) {
        this(clazz.getName());
    }

    public String asJavaPath() {
        return String.join(".", segments);
    }

    @Override
    public String asTypePath() {
        return "%s/%s".formatted(baseName, String.join("/", segments));
    }

    @Override
    public Path asDirPath(Path baseDir) {
        return baseDir.resolve(asTypePath());
    }

    @Override
    public List<String> segments() {
        return segments;
    }

    @Override
    public ClassPath create(String baseName, List<String> segments) {
        return new ClassPath(baseName, segments);
    }

    @HideFromJS
    public Class<?> loadClass() throws ClassNotFoundException {
        if (CLASS_CACHE.containsKey(this)) return CLASS_CACHE.get(this);
        var clazz = Class.forName(asJavaPath(), false, Thread.currentThread().getContextClassLoader());
        CLASS_CACHE.put(this, clazz);
        return clazz;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ClassPath classPath)) return false;
        return Objects.equals(baseName, classPath.baseName) && Objects.equals(segments, classPath.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseName, segments);
    }

    @Override
    public String toString() {
        return asJavaPath();
    }

    public String getBaseName() {
        return baseName;
    }
}
