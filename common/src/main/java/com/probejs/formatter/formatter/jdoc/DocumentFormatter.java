package com.probejs.formatter.formatter.jdoc;

import com.probejs.formatter.formatter.IFormatter;
import com.probejs.jdoc.document.AbstractDocument;

public abstract class DocumentFormatter<T extends AbstractDocument<T>> implements IFormatter {
    protected final T document;

    public DocumentFormatter(T document) {
        this.document = document;
    }
}
