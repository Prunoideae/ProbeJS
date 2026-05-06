package moe.wolfgirl.probejs.typescript.document.members;

import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.base.Type;

import java.util.List;
import java.util.Map;
import java.util.Set;

// Represents a field declaration in a TypeScript class.
// fieldName: type;
public class FieldDecl extends CommentableCode implements KindAware {
    public String name;
    public Type typeInfo;
    public boolean isStatic;
    private KindAware.Kind kind;

    public FieldDecl(String name, Type typeInfo, boolean isStatic) {
        this.name = name;
        this.typeInfo = typeInfo;
        this.isStatic = isStatic;
    }

    @Override
    public void setKind(Kind kind) {
        this.kind = kind;
    }

    @Override
    public Set<ClassPath> getImports() {
        return typeInfo.getImports();
    }

    public String getPrefix() {
        return kind == KindAware.Kind.NAMESPACE ? "let " : isStatic ? "static " : "";
    }

    @Override
    public List<String> format(int indent) {
        return List.of("%s%s%s: %s;".formatted(" ".repeat(indent), getPrefix(), name, typeInfo.first()));
    }

    @Override
    public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
        super.setResolvedSymbols(resolvedSymbols);
        typeInfo.setResolvedSymbols(resolvedSymbols);
    }

    @Override
    public boolean shouldAppear(Kind kind) {
        if (this.kind == Kind.INTERFACE && kind == Kind.CLASS) return isStatic;
        else if (this.kind == kind && kind == Kind.INTERFACE) return !isStatic;
        return true;
    }
}
