package com.probejs.jdoc;

import com.probejs.jdoc.document.AbstractDocument;

public interface IDocumentProvider<T extends AbstractDocument<T>> {
    T genDoc();
}
