package moe.wolfgirl.probejs.typescript.document;

import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.types.VariableType;

import java.util.List;
import java.util.Map;
import java.util.Set;

// export type Identifier = TypeInfo;
public class TypeDecl extends CommentableCode {
    public final ClassPath identifier;
    public final List<VariableType> variables;
    public final Type typeInfo;
    public final boolean export;

    public TypeDecl(ClassPath identifier, List<VariableType> variables, Type typeInfo, boolean export) {
        this.identifier = identifier;
        this.variables = variables;
        this.typeInfo = typeInfo;
        this.export = export;
    }

    public TypeDecl(ClassPath identifier, Type typeInfo, boolean export) {
        this(identifier, List.of(), typeInfo, export);
    }

    public TypeDecl(ClassPath identifier, Type typeInfo) {
        this(identifier, List.of(), typeInfo, true);
    }


    @Override
    public Set<ClassPath> getImports() {
        return typeInfo.getImports();
    }

    @Override
    public List<String> format(int indent) {
        String variablesPart = variables.isEmpty() ? "" : "<%s>".formatted(String.join(", ", variables.stream().map(v -> v.name).toList()));
        return List.of("%s%stype %s%s = %s;".formatted(
                " ".repeat(indent),
                export ? "export " : "",
                identifier.getClassName(),
                variablesPart,
                typeInfo.first()
        ));
    }

    @Override
    public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
        super.setResolvedSymbols(resolvedSymbols);
        typeInfo.setResolvedSymbols(resolvedSymbols);
    }
}
