package moe.wolfgirl.probejs.next.plugin.builtins;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.members.ConstructorInfo;
import moe.wolfgirl.probejs.next.java.members.FieldInfo;
import moe.wolfgirl.probejs.next.java.members.MethodInfo;
import moe.wolfgirl.probejs.next.java.members.other.HasAnnotation;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.Documents;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.next.typescript.document.members.ConstructorDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.MethodDecl;

import java.util.ArrayList;
import java.util.List;

public class InjectAnnotations extends ProbeJSPlugin {
    @Override
    public void transformClass(Documents.ClassDocument document) {
        var classDocument = document.document();
        var classInfo = document.classInfo();
        if (markedHidden(classInfo)) {
            classDocument.members.clear();
            return;
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
}
