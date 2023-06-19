package com.probejs.jdoc;

import com.probejs.jdoc.property.PropertyComment;
import dev.latvian.mods.kubejs.typings.JsInfo;
import dev.latvian.mods.kubejs.typings.JsParam;

public class JsAnnotations {

    public static PropertyComment fromAnnotation(JsInfo info) {
        return fromAnnotation(info, false);
    }

    public static PropertyComment fromAnnotation(JsInfo info, boolean isMethod) {
        PropertyComment comment = new PropertyComment();
        comment.add(info.value());
        if (isMethod) {
            for (JsParam param : info.params()) {
                comment.add("@param " + param.name() + " " + param.value());
            }
        }
        return comment;
    }
}
