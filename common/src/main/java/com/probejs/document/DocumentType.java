package com.probejs.document;

import com.probejs.document.comment.CommentUtil;
import com.probejs.document.parser.processor.IDocumentProvider;
import com.probejs.document.type.IType;
import com.probejs.document.type.Resolver;
import com.probejs.formatter.formatter.IFormatter;

import java.util.ArrayList;
import java.util.List;

public class DocumentType implements IDocumentProvider<DocumentType>, IFormatter, IConcrete {
    //type <name> = <type>;

    private DocumentComment comment;
    private final String name;
    private final IType type;

    public DocumentType(String line) {
        line = line.strip().substring(4).strip();
        if (line.endsWith(";"))
            line = line.substring(0, line.length() - 1);
        String[] nameType = line.split("=");
        name = nameType[0].strip();
        type = Resolver.resolveType(nameType[1].strip());
    }

    @Override
    public DocumentType provide() {
        return this;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        if (!CommentUtil.isLoaded(comment) || CommentUtil.isHidden(comment))
            return new ArrayList<>();
        return List.of(" ".repeat(indent) + "type %s = %s;".formatted(name, type.getTypeName()));
    }

    @Override
    public void acceptDeco(List<IDecorative> decorates) {
        decorates.forEach(d -> {
            if (d instanceof DocumentComment) {
                comment = (DocumentComment) d;
            }
        });
    }

    public IType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public DocumentComment getComment() {
        return comment;
    }
}
