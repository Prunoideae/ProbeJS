package moe.wolfgirl.probejs.next;

import dev.latvian.mods.rhino.util.HideFromJS;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ClassPath {
    private final List<String> segments;

    public ClassPath(List<String> segments) {
        this.segments = segments;
    }

    public ClassPath(String className) {
        this(List.of(className.split("\\.")));
    }

    public ClassPath(Class<?> clazz) {
        this(clazz.getName());
    }

    public List<String> segments() {
        return segments;
    }

    public String getClassName() {
        return segments.getLast();
    }

    public ClassPath getPackage() {
        if (segments.size() <= 1) return null;
        return new ClassPath(segments.subList(0, segments.size() - 1));
    }

    public String asJavaPath() {
        return String.join(".", segments);
    }

    public String asTypePath() {
        return "@package/" + String.join("/", segments);
    }

    public Path asDirPath(Path baseDir) {
        return baseDir.resolve(String.join("/", segments));
    }

    // java.lang.Class -> java.lang.$Class
    public ClassPath withPrefix(String prefix) {
        List<String> newSegments = new java.util.ArrayList<>(segments);
        int lastIndex = newSegments.size() - 1;
        newSegments.set(lastIndex, prefix + newSegments.get(lastIndex));
        return new ClassPath(newSegments);
    }

    public ClassPath withSuffix(String suffix) {
        List<String> newSegments = new java.util.ArrayList<>(segments);
        int lastIndex = newSegments.size() - 1;
        newSegments.set(lastIndex, newSegments.get(lastIndex) + suffix);
        return new ClassPath(newSegments);
    }

    @HideFromJS
    public Class<?> loadClass() throws ClassNotFoundException {
        return Class.forName(asJavaPath(), false, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ClassPath classPath)) return false;
        return Objects.equals(segments, classPath.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(segments);
    }

    @Override
    public String toString() {
        return asJavaPath();
    }
}
