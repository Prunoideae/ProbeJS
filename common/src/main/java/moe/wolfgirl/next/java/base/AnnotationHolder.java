package moe.wolfgirl.next.java.base;

import java.lang.annotation.Annotation;
import java.util.Arrays;

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
}
