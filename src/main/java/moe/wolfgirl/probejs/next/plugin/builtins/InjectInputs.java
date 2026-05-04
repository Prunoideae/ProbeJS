package moe.wolfgirl.probejs.next.plugin.builtins;

import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.Documents;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.members.ConstructorDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.ParamDecl;

import java.util.List;

public class InjectInputs extends ProbeJSPlugin {

    @Override
    public void transformClass(Documents.ClassDocument document) {
        var classDocument = document.document();
        for (Code member : classDocument.members) {
            if (member instanceof MethodDecl methodDecl) patchParams(methodDecl.params);
            if (member instanceof ConstructorDecl constructorDecl) patchParams(constructorDecl.params);
        }
    }

    private void patchParams(List<ParamDecl> paramDecls) {
        for (ParamDecl paramDecl : paramDecls) {
            Types.markAsInput(paramDecl.typeInfo);
        }
    }
}
