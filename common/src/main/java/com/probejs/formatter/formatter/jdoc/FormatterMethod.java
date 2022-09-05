package com.probejs.formatter.formatter.jdoc;

import com.probejs.jdoc.document.DocumentClass;

import java.util.List;

public class FormatterMethod extends DocumentFormatter<DocumentClass> {
    public FormatterMethod(DocumentClass document) {
        super(document);
    }

    @Override
    public List<String> formatDocument(Integer indent, Integer stepIndent) {
        return null;
    }
}
