package moe.wolfgirl.probejs.next.java.members.other;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public interface HasAnnotation {
    Annotation[] getAnnotations();

    default boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        for (Annotation annotation : getAnnotations()) {
            if (annotationClass.isInstance(annotation)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    default <T extends Annotation> List<T> getAnnotations(Class<T> annotationClass) {
        return Arrays.stream(getAnnotations())
                .filter(annotationClass::isInstance)
                .map(annotation -> (T) annotation)
                .toList();
    }

    @Nullable
    default <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return getAnnotations(annotationClass).stream().findFirst().orElse(null);
    }
}
