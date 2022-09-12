package com.probejs.formatter.formatter.jdoc;

import com.probejs.formatter.formatter.IFormatter;
import com.probejs.jdoc.document.AbstractDocument;
import com.probejs.jdoc.property.PropertyComment;

import java.util.ArrayList;
import java.util.List;

public abstract class DocumentFormatter<T extends AbstractDocument<T>> implements IFormatter {
    protected final T document;

    public DocumentFormatter(T document) {
        this.document = document.applyProperties();
    }

    protected abstract List<String> formatDocument(Integer indent, Integer stepIndent);

    public boolean hasComment() {
        return true;
    }

    public boolean canHide() {
        return true;
    }

    @Override
    public final List<String> format(Integer indent, Integer stepIndent) {
        //Apply document-wide changes here
        if (document.isHidden() && canHide())
            return List.of();
        List<String> lines = new ArrayList<>();
        PropertyComment comments = document.getMergedComment();
        if (!comments.isEmpty() && hasComment()) {
            lines.addAll(comments.formatLines(indent));
        }
        lines.addAll(formatDocument(indent, stepIndent));
        return lines;
    }
}
