package com.probejs.document.comment.special;

import com.probejs.document.comment.AbstractComment;

public class CommentTarget extends AbstractComment {
    private final String targetName;

    public CommentTarget(String line) {
        super(line);
        targetName = line.substring(8);
    }

    public String getTargetName() {
        return targetName;
    }
}
