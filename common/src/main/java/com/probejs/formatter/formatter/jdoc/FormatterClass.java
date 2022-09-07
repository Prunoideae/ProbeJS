package com.probejs.formatter.formatter.jdoc;

import com.probejs.formatter.NameResolver;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FormatterClass extends DocumentFormatter<DocumentClass> {
    public FormatterClass(DocumentClass document) {
        super(document);
    }

    @Override
    public List<String> formatDocument(Integer indent, Integer stepIndent) {
        List<String> lines = new ArrayList<>();
        StringBuilder header = new StringBuilder();
        if (document.isAbstract() && !document.isInterface())
            header.append("abstract ");
        header.append(document.isInterface() ? "interface" : "class");

        header.append(" %s ".formatted(NameResolver.getResolvedName(document.getName()).getLastName()));
        if (!document.getGenerics().isEmpty()) {
            header.append("<%s> ".formatted(document.getGenerics().stream()
                    .map(Serde::getTypeFormatter)
                    .map(IFormatter::formatFirst)
                    .collect(Collectors.joining(", "))
            ));
        }

        if (!document.isInterface()) {
            if (document.getParent() != null) {
                header.append("extends %s ".formatted(Serde.getTypeFormatter(document.getParent()).formatFirst()));
            }
            if (!document.getInterfaces().isEmpty()) {
                header.append("implements %s ".formatted(document.getInterfaces().stream()
                        .map(Serde::getTypeFormatter)
                        .map(IFormatter::formatFirst)
                        .collect(Collectors.joining(", "))
                ));
            }
        } else {
            List<PropertyType<?>> parents = new ArrayList<>();
            if (document.getParent() != null) {
                parents.add(document.getParent());
            }
            if (!document.getInterfaces().isEmpty()) {
                parents.addAll(document.getInterfaces());
            }
            if (!parents.isEmpty()) {
                header.append("extends %s".formatted(parents.stream()
                        .map(Serde::getTypeFormatter)
                        .map(IFormatter::formatFirst)
                        .collect(Collectors.joining(", "))
                ));
            }
        }

        header.append("{");
        lines.add(Util.indent(indent) + header);
        document.getConstructors().forEach(constructor -> lines.addAll(new FormatterConstructor(constructor).format(indent + stepIndent, stepIndent)));
        document.getMethods().forEach(method -> lines.addAll(new FormatterMethod(method).format(indent + stepIndent, stepIndent)));
        document.getMethods().stream().map(FormatterMethod::new).map(FormatterMethod::getBeanFormatter).filter(Optional::isPresent).map(Optional::get).forEach(formatter -> lines.addAll(formatter.format(indent + stepIndent, stepIndent)));
        document.getFields().forEach(field -> lines.addAll(new FormatterField(field).format(indent + stepIndent, stepIndent)));
        lines.add(Util.indent(indent) + "}");
        return lines;
    }
}
