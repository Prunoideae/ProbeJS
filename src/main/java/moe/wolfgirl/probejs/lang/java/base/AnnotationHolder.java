package moe.wolfgirl.probejs.lang.java.base;

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
        return Arrays.stream(annotations).anyMatch(annotation::isInstance);
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> List<T> getAnnotations(Class<T> type) {
        return Arrays.stream(annotations)
                .filter(type::isInstance)
                .map(a -> (T) a)
                .toList();
    }

    public <T extends Annotation> T getAnnotation(Class<T> type) {
        var annotations = getAnnotations(type);
        return annotations.isEmpty() ? null : annotations.getFirst();
    }
}
