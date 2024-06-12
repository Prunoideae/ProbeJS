package moe.wolfgirl.probejs.next.java.base;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public class AnnotationHolder {
    private final Annotation[] annotations;

    public AnnotationHolder(Annotation[] annotations) {
        this.annotations = annotations;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return Arrays.stream(annotations).anyMatch(a -> annotation.isAssignableFrom(a.annotationType()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> List<T> getAnnotations(Class<T> type) {
        return Arrays.stream(annotations)
                .filter(type::isInstance)
                .map(a -> (T) a)
                .toList();
    }
}
