package moe.wolfgirl.jdoc;

import moe.wolfgirl.jdoc.java.ClassInfo;
import moe.wolfgirl.jdoc.java.type.ITypeInfo;
import moe.wolfgirl.jdoc.java.type.TypeInfoClass;
import moe.wolfgirl.jdoc.java.type.TypeInfoParameterized;
import moe.wolfgirl.jdoc.property.PropertyAssign;
import moe.wolfgirl.jdoc.property.PropertyComment;
import moe.wolfgirl.specials.special.recipe.component.ComponentConverter;
import moe.wolfgirl.specials.assign.ClassAssignmentManager;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;

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
        Class<?> clazzRaw = clazz.getClazzRaw();
        return ClassAssignmentManager.ASSIGNMENTS.get(clazzRaw).stream()
                .map(ComponentConverter::fromDescription)
                .map(type -> new PropertyAssign().type(type))
                .collect(Collectors.toList());
    }
}
