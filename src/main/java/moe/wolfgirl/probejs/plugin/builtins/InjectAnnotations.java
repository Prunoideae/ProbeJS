package moe.wolfgirl.probejs.plugin.builtins;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.kubejs.typings.ThisIs;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.java.members.ConstructorInfo;
import moe.wolfgirl.probejs.java.members.FieldInfo;
import moe.wolfgirl.probejs.java.members.MethodInfo;
import moe.wolfgirl.probejs.java.members.other.HasAnnotation;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.members.ConstructorDecl;
import moe.wolfgirl.probejs.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.document.types.ClassType;
import moe.wolfgirl.probejs.typescript.document.types.ParamType;

import java.util.*;

public class InjectAnnotations extends ProbeJSPlugin {
    @Override
    public void transformClass(Documents.ClassDocument document) {
        var classDocument = document.document();
        var classInfo = document.classInfo();

        if (markedHidden(classInfo)) {
            classDocument.members.clear();
            return;
        }

        if (classInfo.hasAnnotation(ReturnsSelf.class)) {
            for (Code member : classDocument.members) {
                if (member instanceof MethodDecl methodDecl) {
                    ClassPath classPath = null;
                    if (methodDecl.returnType instanceof ClassType classType) {
                        classPath = classType.classPath;
                    } else if (methodDecl.returnType instanceof ParamType paramType && paramType.baseType instanceof ClassType baseType) {
                        classPath = baseType.classPath;
                    }

                    if (classPath != null && classPath.equals(document.classInfo().classPath())) {
                        methodDecl.returnType = Types.THIS;
                    }
                }
            }
        }

        applyDeprecation(classInfo, classDocument);

        List<Code> toRemove = new ArrayList<>();
        for (Pair<FieldInfo, FieldDecl> fieldDoc : document.fieldDocs()) {
            if (markedHidden(fieldDoc.getFirst())) toRemove.add(fieldDoc.getSecond());
            applyDeprecation(fieldDoc.getFirst(), fieldDoc.getSecond());
        }

        for (Pair<MethodInfo, MethodDecl> methodDoc : document.methodDocs()) {
            if (markedHidden(methodDoc.getFirst())) toRemove.add(methodDoc.getSecond());
            applyDeprecation(methodDoc.getFirst(), methodDoc.getSecond());
            applyReturnThis(methodDoc.getFirst(), methodDoc.getSecond());
            applyThisIs(methodDoc.getFirst(), methodDoc.getSecond());
            applyInfo(methodDoc.getFirst(), methodDoc.getSecond());
        }

        for (Pair<ConstructorInfo, ConstructorDecl> constructorDoc : document.constructorDocs()) {
            if (markedHidden(constructorDoc.getFirst())) toRemove.add(constructorDoc.getSecond());
            applyDeprecation(constructorDoc.getFirst(), constructorDoc.getSecond());
            applyInfo(constructorDoc.getFirst(), constructorDoc.getSecond());
        }

        for (Code code : toRemove) {
            classDocument.members.remove(code);
        }
    }

    private void applyDeprecation(HasAnnotation hasAnnotation, CommentableCode code) {
        if (hasAnnotation.hasAnnotation(Deprecated.class)) {
            code.addComments("@deprecated");
        }
    }

    private void applyReturnThis(HasAnnotation hasAnnotation, MethodDecl methodDecl) {
        if (hasAnnotation.hasAnnotation(ReturnsSelf.class)) {
            methodDecl.returnType = Types.THIS;
        }
    }

    private void applyThisIs(MethodInfo hasAnnotation, MethodDecl methodDecl) {
        if (hasAnnotation.hasAnnotation(ThisIs.class)) {
            ThisIs annotation = hasAnnotation.getAnnotation(ThisIs.class);
            if (annotation == null) return;
            List<ClassPath> classPaths = new ArrayList<>();

            for (Class<?> typeClass : annotation.value()) {
                classPaths.add(new ClassPath(typeClass));
            }

            for (Class<?> typeClass : annotation.classes()) {
                classPaths.add(new ClassPath(typeClass));
            }

            for (String className : annotation.classNames()) {
                classPaths.add(new ClassPath(className));
            }

            if (classPaths.isEmpty()) return;
            var innerType = classPaths.size() == 1 ? Types.clazz(classPaths.getFirst())
                    : Types.union(classPaths.stream().map(Types::clazz).toArray(Type[]::new));
            methodDecl.returnType = Types.wrapped("this is %s", innerType);
        }
    }

    private void applyInfo(HasAnnotation hasAnnotation, CommentableCode code) {
        List<Param> params = new ArrayList<>();
        for (Info annotation : hasAnnotation.getAnnotations(Info.class)) {
            code.addComments(annotation.value());
            params.addAll(List.of(annotation.params()));
        }
        if (!params.isEmpty()) {
            if (code.hasComments()) code.addComments("");
            for (Param param : params) {
                code.addComments("@param %s - %s".formatted(param.name(), param.value()));
            }
        }
    }

    private boolean markedHidden(HasAnnotation hasAnnotation) {
        return hasAnnotation.hasAnnotation(HideFromJS.class);
    }

    public record ThisIsInfo(ClassPath classPath, String methodName, ClassPath isType) {
        public static ThisIsInfo create(Class<?> clazz, String methodName, Class<?> isType) {
            return new ThisIsInfo(new ClassPath(clazz), methodName, new ClassPath(isType));
        }
    }
}
