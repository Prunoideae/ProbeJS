package com.probejs.document.parser.processor;

import com.probejs.document.IDocument;

public interface IDocumentProvider<T extends IDocument> {
    T provide();
}
