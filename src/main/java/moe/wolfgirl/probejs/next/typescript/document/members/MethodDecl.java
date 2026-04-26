package moe.wolfgirl.probejs.next.typescript.document.members;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.next.typescript.document.types.VariableType;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Represents a method declaration in a TypeScript class or interface.
// (static) methodName<T1, T2>(param1: Type1, param2: Type2): ReturnType
public class MethodDecl extends CommentableCode {
    public String name;
    public List<VariableType> typeParams;
    public List<ParamDecl> params;
    @Nullable
    public Code returnType;
    public boolean isStatic;
    public boolean isInterface = false;

    public MethodDecl(String name, List<VariableType> typeParams, List<ParamDecl> params, @Nullable Code returnType, boolean isStatic) {
        this.name = name;
        this.typeParams = typeParams;
        this.params = params;
        this.returnType = returnType;
        this.isStatic = isStatic;
    }

    @Override
    public Set<ClassPath> getImports() {
        Set<ClassPath> imports = new HashSet<>();
        for (Code typeParam : typeParams) {
            imports.addAll(typeParam.getImports());
        }
        for (ParamDecl param : params) {
            imports.addAll(param.getImports());
        }
        if (returnType != null) {
            imports.addAll(returnType.getImports());
        }
        return imports;
    }

    @Override
    public List<String> format(int indent) {
        var indentStr = " ".repeat(indent);
        var staticStr = isStatic ? isInterface ? "function " : "static " : "";
        var typeParamsStr = typeParams.isEmpty() ? "" : "<%s>".formatted(String.join(", ", typeParams.stream().map(VariableType::formatWithBound).toList()));
        var paramsStr = String.join(", ", params.stream().map(Code::first).toList());
        var returnTypeStr = returnType == null ? "" : ": %s".formatted(returnType.first());
        return List.of("%s%s%s%s(%s)%s;".formatted(indentStr, staticStr, name, typeParamsStr, paramsStr, returnTypeStr));
    }
}
