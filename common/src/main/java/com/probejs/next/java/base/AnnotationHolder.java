package com.probejs.next.java.base;

import java.lang.annotation.Annotation;

public class AnnotationHolder {
    private final Annotation[] annotations;
    public AnnotationHolder(Annotation[] annotations) {
        this.annotations = annotations;
    }
    public Annotation[] getAnnotations() {
        return annotations;
    }
}
