package com.probejs.document.comment.special;

import com.probejs.document.comment.AbstractComment;
import com.probejs.document.type.IType;
import com.probejs.document.type.Resolver;

public class CommentAssign extends AbstractComment {
    private final IType type;

    public CommentAssign(String line) {
        super(line);
        type = Resolver.resolveType(line.substring(8));
    }

    public IType getType() {
        return type;
    }
}
