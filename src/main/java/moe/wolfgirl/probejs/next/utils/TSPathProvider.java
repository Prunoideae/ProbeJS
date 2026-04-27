package moe.wolfgirl.probejs.next.utils;

import java.nio.file.Path;
import java.util.List;

public interface TSPathProvider<T extends TSPathProvider<T>> {
    String asTypePath();

    Path asDirPath(Path baseDir);

    List<String> segments();

    String getBaseName();

    T create(String baseName, List<String> segments);

    default String getClassName() {
        return segments().getLast();
    }

    default T getPackage() {
        List<String> segments = segments();
        if (segments.size() <= 1) return null;
        return create(getBaseName(), segments.subList(0, segments.size() - 1));
    }

    default T withPrefix(String prefix) {
        List<String> newSegments = new java.util.ArrayList<>(segments());
        int lastIndex = newSegments.size() - 1;
        newSegments.set(lastIndex, prefix + newSegments.get(lastIndex));
        return create(getBaseName(), newSegments);
    }

    default T withSuffix(String suffix) {
        List<String> newSegments = new java.util.ArrayList<>(segments());
        int lastIndex = newSegments.size() - 1;
        newSegments.set(lastIndex, newSegments.get(lastIndex) + suffix);
        return create(getBaseName(), newSegments);
    }
}
