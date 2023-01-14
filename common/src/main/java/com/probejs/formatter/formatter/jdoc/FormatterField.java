package com.probejs.formatter.formatter.jdoc;

import com.probejs.ProbeJS;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.document.DocumentField;
import com.probejs.jdoc.property.PropertyValue;
import com.probejs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class FormatterField extends DocumentFormatter<DocumentField> {

    public FormatterField(DocumentField document) {
        super(document);
    }

    public boolean shouldFormatValue() {
        return document.isStatic() && document.getValue() != null && !(document.getValue() instanceof PropertyValue.NullValue) && !(Serde.getValueFormatter(document.getValue()) instanceof FormatterValue.FallbackFormatter);
    }

    @Override
    public List<String> formatDocument(Integer indent, Integer stepIndent) {
        List<String> modifiers = new ArrayList<>();
        if (document.isFinal())
            modifiers.add("readonly");
        if (document.isStatic())
            modifiers.add("static");
        return List.of(Util.indent(indent) + "%s%s: %s;".formatted(
                modifiers.isEmpty() ? "" : String.join(" ", modifiers) + " ",
                document.isShouldGSON() ? ProbeJS.GSON.toJson(document.getName()) : document.getName(),
                shouldFormatValue() && !Serde.getValueFormatter(document.getValue()).formatFirst().equals("any") ?
                        Serde.getValueFormatter(document.getValue()).formatFirst() :
                        Serde.getTypeFormatter(document.getType()).formatFirst()
        ));
    }

}
