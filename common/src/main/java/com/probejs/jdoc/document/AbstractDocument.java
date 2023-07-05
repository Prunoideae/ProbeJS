package com.probejs.jdoc.document;

import com.probejs.jdoc.property.PropertyComment;

public abstract class AbstractDocument<T extends AbstractDocument<T>> extends AbstractDocumentBase<T> {
    /**
     * Some lines of built-in comments.
     * <p>
     * They are modified at runtime, and not serialized.
     * <p>
     * Mostly they occur like annotation, tho there might be exceptions.
     * <p>
     * Built-in comments will always appear at the end of the comment.
     */
    protected PropertyComment builtinComments = new PropertyComment();

    public final PropertyComment getMergedComment() {
        PropertyComment comment = new PropertyComment();
        for (PropertyComment partialComment : findPropertiesOf(PropertyComment.class)) {
            comment = comment.merge(partialComment);
        }
        comment = comment.merge(builtinComments);
        return comment;
    }
}
