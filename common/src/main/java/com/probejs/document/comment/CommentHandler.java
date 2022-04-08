package com.probejs.document.comment;

import com.probejs.document.comment.special.*;

import java.util.HashMap;
import java.util.function.Function;

public class CommentHandler {
    public static final HashMap<String, Function<String, AbstractComment>> specialCommentHandler = new HashMap<>();

    public static void init() {
        specialCommentHandler.put("@hidden", CommentHidden::new);
        specialCommentHandler.put("@modify", CommentModify::new);
        specialCommentHandler.put("@target", CommentTarget::new);
        specialCommentHandler.put("@assign", CommentAssign::new);
        specialCommentHandler.put("@mod", CommentMod::new);
        specialCommentHandler.put("@returns", CommentReturns::new);
        specialCommentHandler.put("@rename", CommentRename::new);
    }
}
