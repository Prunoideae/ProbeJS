package com.probejs.document.comment.special;

import com.probejs.document.comment.AbstractComment;
import com.probejs.document.type.IType;
import com.probejs.document.type.Resolver;

public class CommentReturns extends AbstractComment {
    private final IType returnType;

    public CommentReturns(String line) {
        super(line);
        returnType = Resolver.resolveType(line.substring(9));
    }

    public IType getReturnType() {
        return returnType;
    }
}
