package com.probejs.formatter.formatter;

import com.probejs.document.DocumentComment;
import com.probejs.document.DocumentField;
import com.probejs.document.comment.special.CommentHidden;
import com.probejs.formatter.NameResolver;
import com.probejs.info.FieldInfo;

import java.util.ArrayList;
import java.util.List;

public class FormatterField extends DocumentReceiver<DocumentField> implements IFormatter {
    private final FieldInfo fieldInfo;
    private boolean isInterface = false;

    public FormatterField(FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();
        DocumentComment comment = document != null ? document.getComment() : null;
        if (comment != null) {
            if (comment.getSpecialComment(CommentHidden.class) != null)
                return formatted;
            formatted.addAll(comment.format(indent, stepIndent));
        }
        List<String> elements = new ArrayList<>();
        if (fieldInfo.isStatic() && !isInterface)
            elements.add("static");
        if (fieldInfo.isFinal())
            elements.add("readonly");
        elements.add(fieldInfo.getName());
        elements.add(":");

        if (document != null) {
            elements.add(document.getType().getTypeName());
        } else if (fieldInfo.isStatic() && NameResolver.formatValue(fieldInfo.getStaticValue()) != null)
            elements.add(NameResolver.formatValue(fieldInfo.getStaticValue()));
        else
            elements.add(new FormatterType(fieldInfo.getType(), NameResolver.specialTypeGuards.getOrDefault(fieldInfo.getType().getResolvedClass(), true)).format(0, 0));

        formatted.add(" ".repeat(indent) + String.join(" ", elements) + ";");
        return formatted;
    }

    public FieldInfo getFieldInfo() {
        return fieldInfo;
    }
}
