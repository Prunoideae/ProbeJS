package moe.wolfgirl.probejs.typescript.document.members;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.types.VariableType;
import moe.wolfgirl.probejs.utils.NameUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Represents a method declaration in a TypeScript class or interface.
// (static) methodName<T1, T2>(param1: Type1, param2: Type2): ReturnType
public class MethodDecl extends CommentableCode implements KindAware {
    public String name;
    public List<VariableType> typeParams;
    public List<ParamDecl> params;
    public Code returnType;
    public boolean isStatic;
    private KindAware.Kind kind;

    public MethodDecl(String name, List<VariableType> typeParams, List<ParamDecl> params, @Nullable Code returnType, boolean isStatic) {
        this.name = name;
        this.typeParams = typeParams;
        this.params = params;
        this.returnType = returnType;
        this.isStatic = isStatic;
    }

    @Override
    public void setKind(Kind kind) {
        this.kind = kind;
    }

    @Override
    public boolean shouldAppear(Kind kind) {
        if (this.kind == Kind.INTERFACE && kind == Kind.CLASS) return isStatic;
        else if (this.kind == kind && kind == Kind.INTERFACE) return !isStatic;
        return true;
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
        imports.addAll(returnType.getImports());
        return imports;
    }

    public String getPrefix() {
        return kind == KindAware.Kind.NAMESPACE ? "function " : isStatic ? "static " : "";
    }

    @Override
    public List<String> format(int indent) {
        var indentStr = " ".repeat(indent);
        var typeParamsStr = typeParams.isEmpty() ? "" : "<%s>".formatted(String.join(", ", typeParams.stream().map(VariableType::formatWithBound).toList()));
        var paramsStr = String.join(", ", params.stream().map(Code::first).toList());
        var name = NameUtils.isNameSafe(this.name) ? this.name : ProbeJS.GSON.toJson(this.name);
        return List.of("%s%s%s%s(%s): %s;".formatted(indentStr, getPrefix(), name, typeParamsStr, paramsStr, returnType.first()));
    }

    @Override
    public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
        super.setResolvedSymbols(resolvedSymbols);
        for (Code typeParam : typeParams) {
            typeParam.setResolvedSymbols(resolvedSymbols);
        }
        for (ParamDecl param : params) {
            param.setResolvedSymbols(resolvedSymbols);
        }
        returnType.setResolvedSymbols(resolvedSymbols);
    }
}
