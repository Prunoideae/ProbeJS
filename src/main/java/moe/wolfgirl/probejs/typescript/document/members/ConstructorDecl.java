package moe.wolfgirl.probejs.typescript.document.members;

import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.typescript.document.types.VariableType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Represents a constructor declaration in a TypeScript class.
// constructor<T1, T2>(param1: Type1, param2: Type2) { ... }
public class ConstructorDecl extends CommentableCode {
    public List<VariableType> typeParams;
    public List<ParamDecl> params;

    public ConstructorDecl(List<VariableType> typeParams, List<ParamDecl> params) {
        this.typeParams = typeParams;
        this.params = params;
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
        return imports;
    }

    @Override
    public List<String> format(int indent) {
        return List.of("%sconstructor%s(%s);".formatted(
                " ".repeat(indent),
                typeParams.isEmpty() ? "" : "<%s>".formatted(String.join(", ", typeParams.stream().map(VariableType::formatWithBound).toList())),
                String.join(", ", params.stream().map(Code::first).toList())
        ));
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
    }
}
