package com.probejs.formatter.formatter.jdoc;

import com.probejs.formatter.NameResolver;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.util.Util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FormatterMethod extends DocumentFormatter<DocumentMethod> {
    public FormatterMethod(DocumentMethod document) {
        super(document);
    }

    @Override
    public List<String> formatDocument(Integer indent, Integer stepIndent) {
        return List.of(Util.indent(indent) + "%s%s(%s): %s;".formatted(
                document.isStatic() ? "static " : "",
                document.getName(),
                document.getParams().stream()
                        .map(FormatterParam::new)
                        .map(FormatterParam::underscored)
                        .map(IFormatter::formatFirst)
                        .collect(Collectors.joining(", ")),
                Serde.getTypeFormatter(document.getReturns()).formatFirst()));
    }

    public Optional<FormatterBean> getBeanFormatter() {
        String name = document.getName();
        if (name.length() < 4) //get set is
            return Optional.empty();
        FormatterBean bean = null;
        if (name.startsWith("get") && document.getParams().isEmpty() && !Character.isDigit(name.charAt(3)))
            bean = new FormatterObjectGetter(document);
        if (name.startsWith("set") && document.getParams().size() == 1 && !Character.isDigit(name.charAt(3)))
            bean = new FormatterObjectSetter(document);
        if (name.startsWith("is") && document.getParams().isEmpty() && (document.getReturns().getTypeName().equals("bool") || document.getReturns().getTypeName().equals("java.lang.Boolean")) && !Character.isDigit(name.charAt(2)))
            bean = new FormatterIsGetter(document);

        return Optional.ofNullable(bean);
    }

    public static abstract class FormatterBean extends DocumentFormatter<DocumentMethod> {
        public FormatterBean(DocumentMethod document) {
            super(document);
        }

        public static String getCamelCase(String text) {
            return Character.toLowerCase(text.charAt(0)) + text.substring(1);
        }

        protected abstract String getBeanName();
    }

    public static class FormatterObjectGetter extends FormatterBean {

        public FormatterObjectGetter(DocumentMethod document) {
            super(document);
        }

        @Override
        protected List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(Util.indent(indent) + "get %s(): %s".formatted(getBeanName(), Serde.getTypeFormatter(document.getReturns()).formatFirst()));
        }

        @Override
        protected String getBeanName() {
            return getCamelCase(document.getName().substring(3));
        }
    }

    public static class FormatterIsGetter extends FormatterObjectGetter {
        public FormatterIsGetter(DocumentMethod document) {
            super(document);
        }

        @Override
        protected String getBeanName() {
            return getCamelCase(document.getName().substring(2));
        }
    }

    public static class FormatterObjectSetter extends FormatterBean {

        public FormatterObjectSetter(DocumentMethod document) {
            super(document);
        }

        @Override
        protected List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of(Util.indent(indent) + "set %s(%s)".formatted(getBeanName(), new FormatterParam(document.getParams().get(0)).underscored().formatFirst()));
        }

        @Override
        protected String getBeanName() {
            return getCamelCase(document.getName().substring(3));
        }
    }


    public static class FormatterParam extends DocumentFormatter<PropertyParam> {
        private boolean underscored = false;

        @Override
        public boolean canHide() {
            return false;
        }

        @Override
        public boolean hasComment() {
            return false;
        }

        public FormatterParam(PropertyParam document) {
            super(document);
        }

        @Override
        public List<String> formatDocument(Integer indent, Integer stepIndent) {
            return List.of("%s: %s".formatted(NameResolver.getNameSafe(document.getName()), Serde.getTypeFormatter(document.getType()).underscored(underscored).formatFirst()));
        }

        public FormatterParam underscored(boolean flag) {
            underscored = flag;
            return this;
        }

        public FormatterParam underscored() {
            return underscored(true);
        }
    }
}
