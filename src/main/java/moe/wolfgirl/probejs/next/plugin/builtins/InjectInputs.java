package moe.wolfgirl.probejs.next.plugin.builtins;

import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.Documents;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.members.ConstructorDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.ParamDecl;
import moe.wolfgirl.probejs.next.typescript.document.types.ArrayType;
import moe.wolfgirl.probejs.next.typescript.document.types.ClassType;
import moe.wolfgirl.probejs.next.typescript.document.types.ParamType;

import java.lang.reflect.Modifier;
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
            markTypeAsInput(paramDecl.typeInfo);
        }
    }

    private boolean isFunctionalInterface(Type type) {
        if (type instanceof ClassType classType) {
            try {
                Class<?> clazz = classType.classPath.loadClass();
                int abstractMethodCount = 0;
                for (var method : clazz.getMethods()) {
                    if (method.isDefault() || Modifier.isStatic(method.getModifiers())) continue;
                    if (method.getDeclaringClass() == Object.class) continue;
                    abstractMethodCount++;
                }
                return clazz.isInterface() && abstractMethodCount == 1;
            } catch (Throwable t) {
                return false;
            }
        } else return false;
    }

    private void markTypeAsInput(Type type) {
        if (type instanceof ClassType classType) {
            var classPath = classType.classPath;
            // If we have alias, alias will refer to the original type as input, so we don't need to check
            // for functional interface
            if (Documents.INSTANCE.hasAlias(classPath)) classType.markAsInput();
            // if (isFunctionalInterface(type)) classType.markAsOutput();
        } else if (type instanceof ArrayType arrayType) {
            markTypeAsInput(arrayType.componentType);
        } else if (type instanceof ParamType paramType) {
            markTypeAsInput(paramType.baseType);
            // If the type is a functional interface, we don't mark the params as input, as they are likely
            // to be callback and we want original types (so we can get types)
            markTypeAsInput(paramType.baseType);
            if (isFunctionalInterface(paramType.baseType)) return;
            for (Type typeArg : paramType.typeArgs) {
                markTypeAsInput(typeArg);
            }
        }
    }
}
