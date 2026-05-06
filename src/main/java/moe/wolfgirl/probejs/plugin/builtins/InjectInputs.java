package moe.wolfgirl.probejs.plugin.builtins;

import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.members.ConstructorDecl;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.document.members.ParamDecl;

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
