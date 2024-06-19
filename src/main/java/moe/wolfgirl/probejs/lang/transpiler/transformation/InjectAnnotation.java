package moe.wolfgirl.probejs.lang.transpiler.transformation;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import moe.wolfgirl.probejs.lang.java.base.AnnotationHolder;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.java.clazz.members.ConstructorInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.FieldInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
import moe.wolfgirl.probejs.lang.typescript.code.member.*;

import java.util.ArrayList;
import java.util.List;

public class InjectAnnotation implements ClassTransformer {
    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        applyInfo(clazz, classDecl);
        if (clazz.hasAnnotation(Deprecated.class)) classDecl.newline("@deprecated");
    }

    @Override
    public void transformMethod(MethodInfo methodInfo, MethodDecl decl) {
        var params = applyInfo(methodInfo, decl);
        if (methodInfo.hasAnnotation(Deprecated.class)) {
            decl.newline("@deprecated");
        }
        if (!params.isEmpty()) {
            decl.linebreak();
            for (Param param : params) {
                decl.addComment("@param %s - %s".formatted(param.name(), param.value()));
            }
        }
    }

    @Override
    public void transformField(FieldInfo fieldInfo, FieldDecl decl) {
        applyInfo(fieldInfo, decl);
        if (fieldInfo.hasAnnotation(Deprecated.class)) decl.newline("@deprecated");
    }

    @Override
    public void transformConstructor(ConstructorInfo constructorInfo, ConstructorDecl decl) {
        applyInfo(constructorInfo, decl);
        if (constructorInfo.hasAnnotation(Deprecated.class)) decl.newline("@deprecated");
    }

    public List<Param> applyInfo(AnnotationHolder info, CommentableCode decl) {
        List<Param> params = new ArrayList<>();
        for (Info annotation : info.getAnnotations(Info.class)) {
            decl.addComment(annotation.value());
            params.addAll(List.of(annotation.params()));
        }
        return params;
    }
}
