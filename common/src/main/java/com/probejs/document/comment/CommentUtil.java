package com.probejs.document.comment;

import com.probejs.document.DocumentComment;
import com.probejs.document.comment.special.CommentHidden;
import com.probejs.document.comment.special.CommentMod;
import com.probejs.document.comment.special.CommentModify;
import com.probejs.document.comment.special.CommentRename;
import com.probejs.document.type.IType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentUtil {
    public static boolean isLoaded(DocumentComment comment) {
        if (comment == null)
            return true;
        List<CommentMod> mod = comment.getSpecialComments(CommentMod.class).stream().map(c -> (CommentMod) c).collect(Collectors.toList());
        return mod.stream().allMatch(CommentMod::isLoaded);
    }

    public static boolean isHidden(DocumentComment comment) {
        if (comment == null)
            return false;
        return comment.getSpecialComment(CommentHidden.class) != null;
    }

    public static Map<String, IType> getTypeModifiers(DocumentComment comment) {
        Map<String, IType> modifiers = new HashMap<>();
        if (comment != null) {
            comment.getSpecialComments(CommentModify.class).forEach(modify -> modifiers.put(modify.getName(), modify.getType()));
        }
        return modifiers;
    }

    public static Map<String, String> getRenames(DocumentComment comment) {
        Map<String, String> renames = new HashMap<>();
        if (comment != null) {
            comment.getSpecialComments(CommentRename.class).forEach(rename -> renames.put(rename.getName(), rename.getTo()));
        }
        return renames;
    }
}
