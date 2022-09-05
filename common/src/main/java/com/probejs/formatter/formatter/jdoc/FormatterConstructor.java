package com.probejs.formatter.formatter.jdoc;

import com.probejs.jdoc.document.DocumentConstructor;

import java.util.List;

public class FormatterConstructor extends DocumentFormatter<DocumentConstructor> {
    public FormatterConstructor(DocumentConstructor document) {
        super(document);
    }

    @Override
    public List<String> formatDocument(Integer indent, Integer stepIndent) {
        return null;
    }
}
