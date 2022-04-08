package com.probejs.document;

import com.probejs.document.comment.CommentUtil;
import com.probejs.formatter.formatter.IFormatter;

import java.util.ArrayList;
import java.util.List;

public class DocumentClass implements IConcrete, IFormatter {
    private DocumentComment comment;
    private String name;
    private final List<DocumentField> fields = new ArrayList<>();
    private final List<DocumentMethod> methods = new ArrayList<>();

    public DocumentComment getComment() {
        return comment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void acceptProperty(IDocument document) {
        if (document instanceof DocumentProperty) {
            DocumentComment comment = ((DocumentProperty) document).getComment();
            if (!CommentUtil.isLoaded(comment))
                return;
        }

        if (document instanceof DocumentField) {
            fields.add((DocumentField) document);
        }
        if (document instanceof DocumentMethod) {
            methods.add((DocumentMethod) document);
        }
    }

    public void merge(DocumentClass other) {
        if (comment == null)
            comment = other.getComment();
        fields.addAll(other.getFields());
        methods.addAll(other.getMethods());
    }

    public List<DocumentField> getFields() {
        return fields;
    }

    public List<DocumentMethod> getMethods() {
        return methods;
    }

    public String getName() {
        return name;
    }

    @Override
    public void acceptDeco(List<IDecorative> decorates) {
        for (IDecorative decorative : decorates) {
            if (decorative instanceof DocumentComment) {
                this.comment = (DocumentComment) decorative;
            }
        }
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();
        formatted.add(" ".repeat(indent) + "class %s {".formatted(this.name));
        getFields().forEach(f -> formatted.addAll(f.format(indent + stepIndent, stepIndent)));
        getMethods().forEach(m -> formatted.addAll(m.format(indent + stepIndent, stepIndent)));
        formatted.add(" ".repeat(indent) + "}");
        return formatted;
    }
}
