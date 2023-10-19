package com.probejs.docs.formatter.formatter.jdoc;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.probejs.docs.formatter.NameResolver;
import com.probejs.docs.formatter.formatter.IFormatter;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentConstructor;
import com.probejs.jdoc.document.DocumentField;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.property.PropertyAssign;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.util.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FormatterClass extends DocumentFormatter<DocumentClass> {
    public static Multimap<String, Function<DocumentClass, IFormatter>> SPECIAL_FORMATTER_REGISTRY = ArrayListMultimap.create();

    private boolean internal = false;

    public FormatterClass(DocumentClass document) {
        super(document.applyProperties());
    }

    public String getClassGeneric() {
        if (document.getGenerics().isEmpty())
            return "";
        return "<%s>".formatted(document.getGenerics().stream().map(Serde::getTypeFormatter).map(IFormatter::formatParamVariable).collect(Collectors.joining(", ")));
    }

    public String getTypeClassGeneric() {
        if (document.getGenerics().isEmpty())
            return "";
        return "<%s>".formatted(document.getGenerics().stream().map(Serde::getTypeFormatter).map(IFormatter::formatFirst).collect(Collectors.joining(", ")));
    }

    @Override
    public List<String> formatDocument(Integer indent, Integer stepIndent) {
        List<String> lines = new ArrayList<>();
        StringBuilder header = new StringBuilder();
        if (!internal)
            header.append("declare ");
        if (document.isAbstract() && !document.isInterface())
            header.append("abstract ");
        header.append(document.isInterface() ? "interface" : "class");

        header.append(" %s ".formatted(NameResolver.getResolvedName(document.getName()).getLastName()));
        if (!document.getGenerics().isEmpty()) {
            header.append("<%s> ".formatted(document.getGenerics().stream()
                    .map(Serde::getTypeFormatter)
                    .map(IFormatter::formatClassVariable)
                    .collect(Collectors.joining(", "))
            ));
        }

        if (!document.isInterface()) {
            if (document.getParent() != null) {
                header.append("extends %s ".formatted(Serde.getTypeFormatter(document.getParent()).formatParamVariable()));
            }
            if (!document.getInterfaces().isEmpty()) {
                header.append("implements %s ".formatted(document.getInterfaces().stream()
                        .map(Serde::getTypeFormatter)
                        .map(IFormatter::formatParamVariable)
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
                header.append("extends %s ".formatted(parents.stream()
                        .map(Serde::getTypeFormatter)
                        .map(IFormatter::formatParamVariable)
                        .collect(Collectors.joining(", "))
                ));
            }
        }

        header.append("{");
        lines.add(Util.indent(indent) + header);
        document.getConstructors().stream().map(DocumentConstructor::applyProperties).forEach(constructor -> lines.addAll(new FormatterConstructor(constructor).format(indent + stepIndent, stepIndent)));
        document.getMethods().stream().map(DocumentMethod::applyProperties).forEach(method -> lines.addAll(new FormatterMethod(method, document).format(indent + stepIndent, stepIndent)));
        document.getMethods().stream().map(DocumentMethod::applyProperties).map(method -> new FormatterMethod(method, document)).map(FormatterMethod::getBeanFormatter).filter(Optional::isPresent).map(Optional::get).forEach(formatter -> lines.addAll(formatter.format(indent + stepIndent, stepIndent)));

        /* hybrid type only accepts one abstract method on top of the interface
         * we assume that any sub interface will not break the abstract method of parent interface
         * meaning that following interface is not allowed:
         *
         * @FunctionalInterface
         * interface Sus extends Function<DocumentClass, IFormatter> {
         *     @Override
         *     default IFormatter apply(DocumentClass documentClass) {
         *       return foo() ? null : (foo, bar) -> List.of();
         *     }
         *
         *     boolean foo();
         * }
         */
        if (document.isFunctionalInterface()) {
            // Still need to check if there is only one abstract method in case of people going insane
            if (document.methods.stream().filter(method -> method.isAbstract).count() == 1) {
                FormatterMethod hybridFormatter = new FormatterMethod(document.methods.stream().filter(method -> method.isAbstract).findFirst().get(), document);
                hybridFormatter.setFunctionalInterface(true);
                lines.add("%s%s;".formatted(" ".repeat(indent + stepIndent), hybridFormatter.formatMethodParts()));
            }
        }

        document.getFields().stream().map(DocumentField::applyProperties).forEach(field -> lines.addAll(new FormatterField(field).setInterface(document.isInterface()).format(indent + stepIndent, stepIndent)));
        lines.add(Util.indent(indent) + "}");
        Set<String> typesAssignable = new HashSet<>();
        String typeName = NameResolver.getResolvedName(document.getName()).getLastName();
        if (document.findPropertiesOf(PropertyAssign.class).stream().noneMatch(PropertyAssign::isShieldOriginal))
            typesAssignable.add(typeName + getClassGeneric());
        document.findPropertiesOf(PropertyAssign.class).forEach(property -> typesAssignable.add(Serde.getTypeFormatter(property.getType()).underscored().formatParamVariable()));
        for (Function<DocumentClass, IFormatter> formatter : SPECIAL_FORMATTER_REGISTRY.get(document.getName())) {
            typesAssignable.add(formatter.apply(document).formatFirst());
        }
        typesAssignable.addAll(NameResolver.getClassAssignments(document.getName()));
        lines.add(Util.indent(indent) + "type %s_%s = %s;".formatted(typeName, getTypeClassGeneric(), String.join(" | ", typesAssignable)));
        return lines;
    }

    public FormatterClass setInternal(boolean internal) {
        this.internal = internal;
        return this;
    }
}
