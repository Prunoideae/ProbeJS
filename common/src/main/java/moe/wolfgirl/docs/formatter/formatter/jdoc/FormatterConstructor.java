package moe.wolfgirl.docs.formatter.formatter.jdoc;

import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.jdoc.document.DocumentConstructor;
import moe.wolfgirl.util.Util;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterConstructor extends DocumentFormatter<DocumentConstructor> {
    public FormatterConstructor(DocumentConstructor document) {
        super(document);
    }

    @Override
    public List<String> formatDocument(Integer indent, Integer stepIndent) {
        return List.of(Util.indent(indent) + "constructor(%s)".formatted(
                document.getParams()
                        .stream()
                        .map(FormatterMethod.FormatterParam::new)
                        .map(FormatterMethod.FormatterParam::underscored)
                        .map(IFormatter::formatFirst)
                        .collect(Collectors.joining(", "))
        ));
    }
}
