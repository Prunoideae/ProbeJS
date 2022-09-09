package com.probejs.formatter.formatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FormatterNamespace implements IFormatter {
    private final String path;
    private final Collection<? extends IFormatter> formatters;

    public FormatterNamespace(String path, Collection<? extends IFormatter> formatters) {
        this.formatters = formatters;
        this.path = path;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();
        formatted.add(" ".repeat(indent) + "declare namespace %s {".formatted(path));
        for (IFormatter formatter : formatters) {
            formatted.addAll(formatter.format(indent + stepIndent, stepIndent));
        }
        formatted.add(" ".repeat(indent) + "}");
        return formatted;
    }
}
