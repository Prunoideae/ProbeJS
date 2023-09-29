package com.probejs.jdoc;

import com.probejs.jdoc.java.ClassInfo;
import com.probejs.jdoc.java.type.ITypeInfo;
import com.probejs.jdoc.java.type.InfoTypeResolver;
import com.probejs.jdoc.java.type.TypeInfoClass;
import com.probejs.jdoc.java.type.TypeInfoParameterized;
import com.probejs.jdoc.property.PropertyAssign;
import com.probejs.jdoc.property.PropertyComment;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.jdoc.wrapped.ClassType;
import com.probejs.jdoc.wrapped.ListType;
import com.probejs.jdoc.wrapped.LiteralType;
import com.probejs.jdoc.wrapped.ObjectType;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public static ITypeInfo fromGenerics(Generics generics) {
        TypeInfoClass base = new TypeInfoClass(generics.base());
        return generics.value().length == 0 ?
                base :
                new TypeInfoParameterized(
                        base,
                        Arrays.stream(generics.value()).map(clazz -> (ITypeInfo) new TypeInfoClass(clazz)).toList()
                );
    }

    public static List<PropertyAssign> getClassAssignments(ClassInfo clazz) {
        List<PropertyAssign> assigns = new ArrayList<>();
        Class<?> clazzRaw = clazz.getClazzRaw();
        for (ClassType clazzType : clazzRaw.getAnnotationsByType(ClassType.class)) {
            assigns.add(new PropertyAssign().type(Serde.deserializeFromJavaType(InfoTypeResolver.resolveType(clazzType.value()))));
        }
        for (LiteralType literalType : clazzRaw.getAnnotationsByType(LiteralType.class)) {
            assigns.add(new PropertyAssign().type(new PropertyType.Native(literalType.value())));
        }
        for (ListType listType : clazzRaw.getAnnotationsByType(ListType.class)) {
            assigns.add(new PropertyAssign().type(
                    new PropertyType.JSArray(
                            Arrays.stream(listType.value())
                                    .map(cl -> Serde.deserializeFromJavaType(InfoTypeResolver.resolveType(cl.value())))
                                    .collect(Collectors.toList())
                    )
            ));
        }
      for (ObjectType objectType : clazzRaw.getAnnotationsByType(ObjectType.class)) {
            PropertyType.JSObject jsObject = new PropertyType.JSObject();
            for (int i = 0; i < objectType.names().length; i++) {
                var name = objectType.names()[i];
                var optional = name.endsWith("?");
                if (optional) {
                    name = name.substring(0, name.length() - 1);
                }
                var type = objectType.types()[i];
                jsObject.add(
                        new PropertyType.JSObjectKey().optional(optional).withName(name),
                        Serde.deserializeFromJavaType(InfoTypeResolver.resolveType(type.value()))
                );
            }
        }
        return assigns;
    }
}
