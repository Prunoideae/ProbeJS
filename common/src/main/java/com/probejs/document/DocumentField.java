package com.probejs.document;

import com.probejs.document.parser.processor.IDocumentProvider;
import com.probejs.document.type.IType;
import com.probejs.document.type.Resolver;
import com.probejs.formatter.formatter.IFormatter;

import java.util.ArrayList;
import java.util.List;

public class DocumentField extends DocumentProperty implements IDocumentProvider<DocumentField>, IFormatter {
    private final boolean isFinal;
    private final boolean isStatic;
    private final String name;
    private final IType type;

    public DocumentField(String line) {
        line = line.strip();
        boolean f = false;
        boolean s = false;
        boolean flag = true;
        while (flag) {
            if (line.startsWith("readonly")) {
                line = line.substring(8).strip();
                f = true;
            } else if (line.startsWith("static")) {
                line = line.substring(6).strip();
                s = true;
            } else {
                flag = false;
            }
        }
        String[] parts = line.split(":");
        name = parts[0].strip();
        type = Resolver.resolveType(parts[1].strip());

        isFinal = f;
        isStatic = s;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getName() {
        return name;
    }

    public IType getType() {
        return type;
    }

    @Override
    public DocumentField provide() {
        return this;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();
        if (comment != null)
            formatted.addAll(comment.format(indent, stepIndent));
        List<String> pre = new ArrayList<>();
        if (isStatic)
            pre.add("static");
        if (isFinal)
            pre.add("readonly");
        pre.add("%s: %s;".formatted(name, type.getTypeName()));
        formatted.add(" ".repeat(indent) + String.join(" ", pre));

        return formatted;
    }
}
