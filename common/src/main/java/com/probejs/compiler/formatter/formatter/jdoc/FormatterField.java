package com.probejs.compiler.formatter.formatter.jdoc;

import com.probejs.jdoc.Serde;
import com.probejs.jdoc.document.DocumentField;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.jdoc.property.PropertyValue;
import com.probejs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class FormatterField extends DocumentFormatter<DocumentField> {

    private boolean isInterface = false;

    public FormatterField(DocumentField document) {
        super(document);
    }

    public FormatterField setInterface(boolean anInterface) {
        isInterface = anInterface;
        return this;
    }

    public boolean shouldFormatValue() {
        //Of course
        if (!document.isStatic() || document.getValue() == null || document.getValue() instanceof PropertyValue.NullValue)
            return false;
        //If we're resolving the type by value, ensure the value type is not parameterized to avoid type erasure
        if (Serde.getValueFormatter(document.getValue()) instanceof FormatterValue.FallbackFormatter fallback)
            return !(fallback.getType() instanceof PropertyType.Parameterized);
        return true;
    }

    @Override
    public List<String> formatDocument(Integer indent, Integer stepIndent) {
        List<String> modifiers = new ArrayList<>();
        if (document.isFinal())
            modifiers.add("readonly");
        if (document.isStatic() && !this.isInterface) // TS interfaces don't support static fields
            modifiers.add("static");
        return List.of(Util.indent(indent) + "%s%s: %s;".formatted(
                modifiers.isEmpty() ? "" : String.join(" ", modifiers) + " ",
                Util.getSafeName(document.getName()),
                shouldFormatValue() && !Serde.getValueFormatter(document.getValue()).formatFirst().equals("any") ?
                        Serde.getValueFormatter(document.getValue()).formatFirst() :
                        Serde.getTypeFormatter(document.getType()).formatFieldVariable()
        ));
    }

}
