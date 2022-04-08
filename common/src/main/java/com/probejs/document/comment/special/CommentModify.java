package com.probejs.document.comment.special;


import com.probejs.document.comment.AbstractComment;
import com.probejs.document.type.IType;
import com.probejs.document.type.Resolver;

public class CommentModify extends AbstractComment {
    private final String name;
    private final IType type;

    public CommentModify(String line) {
        super(line);
        String sub = line.substring(8);
        int idx = sub.indexOf(" ");
        name = sub.substring(0, idx).strip();
        type = Resolver.resolveType(sub.substring(idx + 1));
    }

    public String getName() {
        return name;
    }

    public IType getType() {
        return type;
    }
}
