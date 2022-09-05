package com.probejs.formatter.formatter.jdoc;

import com.probejs.jdoc.document.DocumentField;

import java.util.List;

public class FormatterField extends DocumentFormatter<DocumentField> {
    public FormatterField(DocumentField document) {
        super(document);
    }

    @Override
    public List<String> formatDocument(Integer indent, Integer stepIndent) {
        String head = "";
        return List.of("");
    }

}
