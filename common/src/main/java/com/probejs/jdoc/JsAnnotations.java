package com.probejs.jdoc;

import com.probejs.jdoc.property.PropertyComment;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;

public class JsAnnotations {

    public static PropertyComment fromAnnotation(Info info) {
        return fromAnnotation(info, false);
    }

    public static PropertyComment fromAnnotation(Info info, boolean isMethod) {
        PropertyComment comment = new PropertyComment();
        for (String l : info.value().split("\n")) {
            comment.add(l);
        }
        if (isMethod) {
            for (Param param : info.params()) {
                comment.add("@param " + param.name() + " " + param.value());
            }
        }
        return comment;
    }
}
