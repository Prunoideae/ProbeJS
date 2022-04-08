package com.probejs.document.parser.processor;

import com.probejs.document.parser.handler.AbstractStackedMachine;

public class Document extends AbstractStackedMachine<String> {
    private final DocumentHandler document;

    public Document() {
        document = new DocumentHandler();
        this.stack.add(document);
    }

    @Override
    public void step(String element) {
        super.step(element);
    }

    public DocumentHandler getDocument() {
        return document;
    }
}
