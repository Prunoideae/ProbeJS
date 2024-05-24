package moe.wolfgirl.docs.formatter.formatter.jdoc;

import moe.wolfgirl.jdoc.Serde;
import moe.wolfgirl.jdoc.document.DocumentField;
import moe.wolfgirl.jdoc.property.PropertyType;
import moe.wolfgirl.jdoc.property.PropertyValue;
import moe.wolfgirl.util.Util;

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
        if (document.isStatic() && !this.isInterface) // TS interfaces don't support static fields
            modifiers.add("static");
        if (document.isFinal())
            modifiers.add("readonly");

        return List.of(Util.indent(indent) + "%s%s: %s;".formatted(
                modifiers.isEmpty() ? "" : String.join(" ", modifiers) + " ",
                Util.getSafeName(document.getName()),
                shouldFormatValue() && !Serde.getValueFormatter(document.getValue()).formatFirst().equals("any") ?
                        "(%s) & (%s)".formatted(
                                Serde.getValueFormatter(document.getValue()).formatFirst(),
                                Serde.getTypeFormatter(document.getType()).formatFieldVariable()
                        ) :
                        Serde.getTypeFormatter(document.getType()).formatFieldVariable()
        ));
    }

}
